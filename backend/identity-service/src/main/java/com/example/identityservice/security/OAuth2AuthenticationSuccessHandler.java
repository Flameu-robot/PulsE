package com.example.identityservice.security;

import com.example.identityservice.config.OAuth2Properties;
import com.example.identityservice.dto.OAuth2UserInfo;
import com.example.identityservice.entity.RefreshToken;
import com.example.identityservice.entity.User;
import com.example.identityservice.entity.enums.OAuthProvider;
import com.example.identityservice.repository.TokenRepository;
import com.example.identityservice.service.JwtService;
import com.example.identityservice.service.OAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Objects;

@Component
public class OAuth2AuthenticationSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(
            OAuth2AuthenticationSuccessHandler.class
    );

    private final OAuth2UserService oAuth2UserService;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final OAuth2Properties oAuth2Properties;

    public OAuth2AuthenticationSuccessHandler(
            OAuth2UserService oAuth2UserService,
            JwtService jwtService,
            TokenRepository tokenRepository,
            OAuth2Properties oAuth2Properties
    ) {
        this.oAuth2UserService = oAuth2UserService;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.oAuth2Properties = oAuth2Properties;
    }

    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Authentication authentication
    ) throws IOException {

        OAuth2AuthenticationToken authToken =
                (OAuth2AuthenticationToken) authentication;

        String registrationId = authToken.getAuthorizedClientRegistrationId();
        OAuth2User oAuth2User = authToken.getPrincipal();

        OAuthProvider provider = OAuthProvider.valueOf(registrationId.toUpperCase());
        OAuth2UserInfo userInfo = OAuth2UserInfo.of(
                provider, Objects.requireNonNull(oAuth2User).getAttributes()
        );

        User user = oAuth2UserService.processOAuth2User(userInfo);

        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getUsername(), user.getRole().name()
        );
        String refreshToken = jwtService.generateRefreshToken(
                user.getId(), user.getUsername()
        );

        RefreshToken tokenEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(refreshToken))
                .expiresAt(OffsetDateTime.now().plusSeconds(
                        jwtService.getRefreshTokenExpiration() / 1000
                ))
                .userAgent(request.getHeader("User-Agent"))
                .ipAddress(request.getRemoteAddr())
                .build();

        tokenRepository.save(tokenEntity);

        String targetUrl = UriComponentsBuilder
                .fromUriString(oAuth2Properties.defaultRedirectUri())
                .queryParam("access_token", accessToken)
                .queryParam("refresh_token", refreshToken)
                .build()
                .toUriString();

        log.info("OAuth2 success: user={}, provider={}", user.getUsername(), provider);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
