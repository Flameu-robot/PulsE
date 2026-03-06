package com.example.identityservice.service;

import com.example.identityservice.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateAccessToken(Long userId, String username, String role) {
        return buildToken(
                userId,
                username,
                Map.of("role", role, "type", "access"),
                jwtProperties.accessToken().expiration()
        );
    }

    public String generateRefreshToken(Long userId, String username) {
        return buildToken(
                userId,
                username,
                Map.of("type", "refresh"),
                jwtProperties.refreshToken().expiration()
        );
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractTokenType(String token) {
        return extractClaims(token).get("type", String.class);
    }

    public long getRefreshTokenExpiration() {
        return jwtProperties.refreshToken().expiration();
    }

    private String buildToken(Long userId, String username, Map<String, Object> claims, long expiration) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(username)
                .id(UUID.randomUUID().toString())
                .claim("userId", userId)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();
    }
}
