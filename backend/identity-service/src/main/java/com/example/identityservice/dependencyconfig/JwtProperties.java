package com.example.identityservice.dependencyconfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        Token accessToken,
        Token refreshToken
) {
    public record Token(long expiration) {}
}