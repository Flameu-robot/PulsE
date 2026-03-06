package com.example.identityservice.repository;

import com.example.identityservice.entity.WebAuthnCredential;
import com.example.identityservice.entity.enums.Transport;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.AuthenticatorTransport;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WebAuthnCredentialRepository implements CredentialRepository {

    private final WebAuthRepository webAuthRepository;
    private final UserRepository userRepository;

    public WebAuthnCredentialRepository(
            WebAuthRepository webAuthRepository,
            UserRepository userRepository
    ) {
        this.webAuthRepository = webAuthRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> webAuthRepository.findAllByUserId(user.getId()).stream()
                        .map(cred -> PublicKeyCredentialDescriptor.builder()
                                .id(new ByteArray(cred.getCredentialId()))
                                .transports(mapTransports(cred.getTransports()))
                                .build()
                        )
                        .collect(Collectors.toSet())
                )
                .orElse(Set.of());
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        long userId = bytesToLong(userHandle.getBytes());
        return userRepository.findById(userId).map(u -> u.getUsername());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> new ByteArray(longToBytes(user.getId())));
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return webAuthRepository.findByCredentialId(credentialId.getBytes())
                .filter(cred -> {
                    if (userHandle != null) {
                        long handleUserId = bytesToLong(userHandle.getBytes());
                        return cred.getUser().getId().equals(handleUserId);
                    }
                    return true;
                })
                .map(this::toRegisteredCredential);
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return webAuthRepository.findByCredentialId(credentialId.getBytes())
                .map(this::toRegisteredCredential)
                .map(Set::of)
                .orElse(Set.of());
    }

    private RegisteredCredential toRegisteredCredential(WebAuthnCredential cred) {
        return RegisteredCredential.builder()
                .credentialId(new ByteArray(cred.getCredentialId()))
                .userHandle(new ByteArray(longToBytes(cred.getUser().getId())))
                .publicKeyCose(new ByteArray(cred.getPublicKey()))
                .signatureCount(cred.getSignCount())
                .build();
    }

    private Set<AuthenticatorTransport> mapTransports(Set<Transport> transports) {
        if (transports == null || transports.isEmpty()) {
            return Set.of();
        }
        return transports.stream()
                .map(t -> switch (t) {
                    case USB -> AuthenticatorTransport.USB;
                    case NFC -> AuthenticatorTransport.NFC;
                    case INTERNAL -> AuthenticatorTransport.INTERNAL;
                    case HYBRID -> AuthenticatorTransport.HYBRID;
                })
                .collect(Collectors.toSet());
    }

    public static byte[] longToBytes(long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    static long bytesToLong(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getLong();
    }
}
