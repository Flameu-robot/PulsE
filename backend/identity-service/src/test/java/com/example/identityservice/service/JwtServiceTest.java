package com.example.identityservice.service;

import com.example.identityservice.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "test-super-secret-key-that-is-at-least-256-bits-long-for-hmac-sha256-algorithm",
                "pulse-test",
                new JwtProperties.AccessToken(900000),
                new JwtProperties.RefreshToken(2592000000L)
        );
        jwtService = new JwtService(properties);
    }

    @Nested
    @DisplayName("generateAccessToken")
    class GenerateAccessToken {

        @Test
        @DisplayName("should generate valid access token")
        void shouldGenerateValidAccessToken() {
            String token = jwtService.generateAccessToken(1L, "testuser", "USER");

            assertThat(token).isNotBlank();
            assertThat(jwtService.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("should contain correct claims")
        void shouldContainCorrectClaims() {
            String token = jwtService.generateAccessToken(1L, "testuser", "USER");

            Claims claims = jwtService.extractClaims(token);

            assertThat(claims.getSubject()).isEqualTo("testuser");
            assertThat(claims.get("userId", Long.class)).isEqualTo(1L);
            assertThat(claims.get("role", String.class)).isEqualTo("USER");
            assertThat(claims.get("type", String.class)).isEqualTo("access");
            assertThat(claims.getIssuer()).isEqualTo("pulse-test");
        }
    }

    @Nested
    @DisplayName("generateRefreshToken")
    class GenerateRefreshToken {

        @Test
        @DisplayName("should generate valid refresh token")
        void shouldGenerateValidRefreshToken() {
            String token = jwtService.generateRefreshToken(1L, "testuser");

            assertThat(token).isNotBlank();
            assertThat(jwtService.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("should have type refresh")
        void shouldHaveTypeRefresh() {
            String token = jwtService.generateRefreshToken(1L, "testuser");

            assertThat(jwtService.extractTokenType(token)).isEqualTo("refresh");
        }
    }

    @Nested
    @DisplayName("isTokenValid")
    class IsTokenValid {

        @Test
        @DisplayName("should return false for invalid token")
        void shouldReturnFalseForInvalidToken() {
            assertThat(jwtService.isTokenValid("invalid.token.here")).isFalse();
        }

        @Test
        @DisplayName("should return false for null token")
        void shouldReturnFalseForNullToken() {
            assertThat(jwtService.isTokenValid(null)).isFalse();
        }

        @Test
        @DisplayName("should return false for empty token")
        void shouldReturnFalseForEmptyToken() {
            assertThat(jwtService.isTokenValid("")).isFalse();
        }

        @Test
        @DisplayName("should return false for token with wrong secret")
        void shouldReturnFalseForTokenWithWrongSecret() {
            JwtProperties otherProperties = new JwtProperties(
                    "other-secret-key-that-is-at-least-256-bits-long-for-hmac-sha256-algorithm!!",
                    "pulse-test",
                    new JwtProperties.AccessToken(900000),
                    new JwtProperties.RefreshToken(2592000000L)
            );
            JwtService otherService = new JwtService(otherProperties);
            String token = otherService.generateAccessToken(1L, "testuser", "USER");

            assertThat(jwtService.isTokenValid(token)).isFalse();
        }
    }

    @Nested
    @DisplayName("extractors")
    class Extractors {

        @Test
        @DisplayName("should extract userId")
        void shouldExtractUserId() {
            String token = jwtService.generateAccessToken(42L, "testuser", "USER");

            assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
        }

        @Test
        @DisplayName("should extract username")
        void shouldExtractUsername() {
            String token = jwtService.generateAccessToken(1L, "myname", "USER");

            assertThat(jwtService.extractUsername(token)).isEqualTo("myname");
        }

        @Test
        @DisplayName("should extract token type")
        void shouldExtractTokenType() {
            String accessToken = jwtService.generateAccessToken(1L, "user", "USER");
            String refreshToken = jwtService.generateRefreshToken(1L, "user");

            assertThat(jwtService.extractTokenType(accessToken)).isEqualTo("access");
            assertThat(jwtService.extractTokenType(refreshToken)).isEqualTo("refresh");
        }
    }
}
