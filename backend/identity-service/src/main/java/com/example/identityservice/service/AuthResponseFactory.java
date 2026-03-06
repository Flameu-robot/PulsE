package com.example.identityservice.service;

import com.example.identityservice.dto.response.AuthResponse;
import com.example.identityservice.entity.RefreshToken;
import com.example.identityservice.entity.User;
import com.example.identityservice.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;

@Component
public class AuthResponseFactory {

    private final JwtService jwtService;
    private final TokenRepository tokenRepository;

    public AuthResponseFactory(JwtService jwtService, TokenRepository tokenRepository) {
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
    }

    public AuthResponse create(User user, HttpServletRequest httpRequest) {
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
                .userAgent(httpRequest != null ? httpRequest.getHeader("User-Agent") : null)
                .ipAddress(httpRequest != null ? httpRequest.getRemoteAddr() : null)
                .build();

        tokenRepository.save(tokenEntity);

        return new AuthResponse(
                accessToken, refreshToken,
                user.getId(), user.getUsername(), user.getRole().name()
        );
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}