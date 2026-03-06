package com.example.identityservice.service;

import com.example.identityservice.dto.response.AuthResponse;
import com.example.identityservice.entity.User;
import com.example.identityservice.entity.WebAuthnCredential;
import com.example.identityservice.entity.enums.Transport;
import com.example.identityservice.repository.UserRepository;
import com.example.identityservice.repository.WebAuthRepository;
import com.example.identityservice.repository.WebAuthnCredentialRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import exception.auth.TokenException;
import exception.auth.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WebAuthnService {

    private static final Logger log = LoggerFactory.getLogger(WebAuthnService.class);

    private final RelyingParty relyingParty;
    private final WebAuthnChallengeStore challengeStore;
    private final WebAuthRepository webAuthRepository;
    private final UserRepository userRepository;
    private final AuthResponseFactory authResponseFactory;

    public WebAuthnService(
            RelyingParty relyingParty,
            WebAuthnChallengeStore challengeStore,
            WebAuthRepository webAuthRepository,
            UserRepository userRepository,
            AuthResponseFactory authResponseFactory
    ) {
        this.relyingParty = relyingParty;
        this.challengeStore = challengeStore;
        this.webAuthRepository = webAuthRepository;
        this.userRepository = userRepository;
        this.authResponseFactory = authResponseFactory;
    }

    public RegistrationStartResponse startRegistration(String username) throws JsonProcessingException {
        User user = findUser(username);

        UserIdentity userIdentity = UserIdentity.builder()
                .name(user.getUsername())
                .displayName(user.getUsername())
                .id(new ByteArray(WebAuthnCredentialRepository.longToBytes(user.getId())))
                .build();

        StartRegistrationOptions options = StartRegistrationOptions.builder()
                .user(userIdentity)
                .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                        .residentKey(ResidentKeyRequirement.PREFERRED)
                        .userVerification(UserVerificationRequirement.PREFERRED)
                        .build()
                )
                .build();

        PublicKeyCredentialCreationOptions creationOptions = relyingParty.startRegistration(options);

        String json = creationOptions.toCredentialsCreateJson();
        String flowId = challengeStore.save(json);

        log.debug("WebAuthn registration started for user {}, flowId={}", username, flowId);
        return new RegistrationStartResponse(flowId, creationOptions);
    }

    @Transactional
    public void finishRegistration(String username, String flowId,
                                   String credentialJson, String deviceName) {
        User user = findUser(username);

        String optionsJson = challengeStore.getAndRemove(flowId);

        // Yubico встроенная десериализация
        PublicKeyCredentialCreationOptions creationOptions;
        try {
            creationOptions = PublicKeyCredentialCreationOptions.fromJson(optionsJson);
        } catch (Exception e) {
            throw new TokenException("Invalid registration options");
        }

        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc;
        try {
            pkc = PublicKeyCredential.parseRegistrationResponseJson(credentialJson);
        } catch (IOException e) {
            throw new TokenException("Invalid registration response");
        }

        FinishRegistrationOptions finishOptions = FinishRegistrationOptions.builder()
                .request(creationOptions)
                .response(pkc)
                .build();

        RegistrationResult result;
        try {
            result = relyingParty.finishRegistration(finishOptions);
        } catch (RegistrationFailedException e) {
            log.warn("WebAuthn registration failed for user {}: {}", username, e.getMessage());
            throw new TokenException("WebAuthn registration validation failed");
        }

        Set<Transport> transports = pkc.getResponse().getTransports()
                .stream()
                .map(this::mapTransport)
                .filter(t -> t != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        java.util.UUID aaguidUuid = extractAaguid(result);

        WebAuthnCredential credential = WebAuthnCredential.builder()
                .user(user)
                .credentialId(result.getKeyId().getId().getBytes())
                .publicKey(result.getPublicKeyCose().getBytes())
                .signCount(result.getSignatureCount())
                .transports(transports)
                .aaguid(aaguidUuid)
                .deviceName(deviceName != null ? deviceName : "Security Key")
                .build();

        webAuthRepository.save(credential);
        log.info("WebAuthn credential registered for user {}, device: {}",
                username, credential.getDeviceName());
    }

    public AuthenticationStartResponse startAuthentication(String username) throws JsonProcessingException {
        StartAssertionOptions.StartAssertionOptionsBuilder builder = StartAssertionOptions.builder()
                .userVerification(UserVerificationRequirement.PREFERRED);

        if (username != null && !username.isBlank()) {
            builder.username(username);
        }

        AssertionRequest assertionRequest = relyingParty.startAssertion(builder.build());

        String json = assertionRequest.toCredentialsGetJson();
        String flowId = challengeStore.save(json);

        log.debug("WebAuthn authentication started, flowId={}, passwordless={}",
                flowId, username == null);
        return new AuthenticationStartResponse(flowId, assertionRequest);
    }

    @Transactional
    public AuthResponse finishAuthentication(String flowId, String credentialJson,
                                             HttpServletRequest httpRequest) {
        String requestJson = challengeStore.getAndRemove(flowId);

        AssertionRequest assertionRequest;
        try {
            assertionRequest = AssertionRequest.fromJson(requestJson);
        } catch (Exception e) {
            throw new TokenException("Invalid assertion request");
        }

        PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc;
        try {
            pkc = PublicKeyCredential.parseAssertionResponseJson(credentialJson);
        } catch (IOException e) {
            throw new TokenException("Invalid assertion response");
        }

        FinishAssertionOptions finishOptions = FinishAssertionOptions.builder()
                .request(assertionRequest)
                .response(pkc)
                .build();

        AssertionResult result;
        try {
            result = relyingParty.finishAssertion(finishOptions);
        } catch (AssertionFailedException e) {
            log.warn("WebAuthn authentication failed: {}", e.getMessage());
            throw new TokenException("WebAuthn authentication failed");
        }

        if (!result.isSuccess()) {
            throw new TokenException("WebAuthn authentication failed");
        }

        WebAuthnCredential credential = webAuthRepository
                .findByCredentialId(result.getCredential().getCredentialId().getBytes())
                .orElseThrow(() -> new TokenException("Credential not found"));

        credential.setSignCount(result.getSignatureCount());
        webAuthRepository.save(credential);

        User user = credential.getUser();
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        log.info("WebAuthn authentication successful for user {}", user.getUsername());
        return authResponseFactory.create(user, httpRequest);
    }

    @Transactional
    public void removeCredential(String username, Long credentialId) {
        User user = findUser(username);
        WebAuthnCredential credential = webAuthRepository.findById(credentialId)
                .orElseThrow(() -> new TokenException("Credential not found"));

        if (!credential.getUser().getId().equals(user.getId())) {
            throw new TokenException("Credential does not belong to user");
        }

        boolean hasPassword = user.getPasswordHash() != null;
        long count = webAuthRepository.findAllByUserId(user.getId()).size();
        if (!hasPassword && count <= 1) {
            throw new TokenException(
                    "Cannot remove the only authentication method. Set a password first."
            );
        }

        webAuthRepository.delete(credential);
        log.info("WebAuthn credential {} removed for user {}", credentialId, username);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    private Transport mapTransport(AuthenticatorTransport t) {
        if (t == null) return null;
        String id = t.getId();
        return switch (id) {
            case "usb" -> Transport.USB;
            case "nfc" -> Transport.NFC;
            case "internal" -> Transport.INTERNAL;
            case "hybrid" -> Transport.HYBRID;
            default -> {
                log.debug("Unknown WebAuthn transport: {}", id);
                yield null;
            }
        };
    }

    private java.util.UUID extractAaguid(RegistrationResult result) {
        try {
            ByteArray aaguidBytes = result.getAaguid();
            if (aaguidBytes == null || aaguidBytes.isEmpty()) return null;
            String hex = aaguidBytes.getHex();
            if (hex.equals("00000000000000000000000000000000")) return null;
            String formatted = hex.substring(0, 8) + "-"
                    + hex.substring(8, 12) + "-"
                    + hex.substring(12, 16) + "-"
                    + hex.substring(16, 20) + "-"
                    + hex.substring(20);
            return java.util.UUID.fromString(formatted);
        } catch (Exception e) {
            log.debug("Could not extract AAGUID: {}", e.getMessage());
            return null;
        }
    }


    public record RegistrationStartResponse(
            String flowId,
            PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions
    ) {}

    public record AuthenticationStartResponse(
            String flowId,
            AssertionRequest assertionRequest
    ) {}
}