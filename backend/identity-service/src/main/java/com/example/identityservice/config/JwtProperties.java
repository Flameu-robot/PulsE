package com.example.identityservice.config;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        String issuer,
        AccessToken accessToken,
        RefreshToken refreshToken
) {
    public record AccessToken(long expiration) {}
    public record RefreshToken(long expiration) {}
}
