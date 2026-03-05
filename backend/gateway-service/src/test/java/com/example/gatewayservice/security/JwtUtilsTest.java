package com.example.gatewayservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Field;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtUtils Tests")
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    private static final String SECRET = "my-super-secret-key-for-testing-purposes-1234567890";
    private SecretKey key;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtils = new JwtUtils();

        // Инжекция secret через рефлексию
        Field secretField = JwtUtils.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtUtils, SECRET);

        jwtUtils.init();

        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String buildToken(Date expiration) {
        return Jwts.builder()
                .subject("Ziragon")
                .claim("userId", 3)
                .claim("role", "USER")
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    private String buildValidToken() {
        return buildToken(
                new Date(System.currentTimeMillis() + 60_000) // +1 минута
        );
    }

    @Test
    @DisplayName("Valid token")
    void shouldReturnTrueForValidToken() {
        String token = buildValidToken();

        assertThat(jwtUtils.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Expired token")
    void shouldReturnFalseForExpiredToken() {
        String token = buildToken(
                new Date(System.currentTimeMillis() - 1000) // Истекший токен
        );

        assertThat(jwtUtils.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("Tampered token")
    void shouldReturnFalseForTamperedToken() {
        String token = buildValidToken();
        // Порча последнего символа
        String tampered = token.substring(0, token.length() - 1) + "X";

        assertThat(jwtUtils.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("Token signed with wrong key")
    void shouldReturnFalseForWrongKey() {
        SecretKey wrongKey = Keys.hmacShaKeyFor(
                "another-secret-key-that-is-long-enough-1234567890".getBytes(StandardCharsets.UTF_8)
        );

        String token = Jwts.builder()
                .subject("Ziragon")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(wrongKey)
                .compact();

        assertThat(jwtUtils.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("Valid token with correct claims data")
    void shouldParseClaimsCorrectly() {
        String token = buildValidToken();

        Claims claims = jwtUtils.getAllClaimsFromToken(token);

        assertThat(claims.getSubject()).isEqualTo("Ziragon");
        assertThat(claims.get("userId", Integer.class)).isEqualTo(3);
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
    }

    @Test
    @DisplayName("Invalid token with incorrect claims data")
    void shouldThrowOnInvalidToken() {
        assertThatThrownBy(() -> jwtUtils.getAllClaimsFromToken("invalid"))
                .isInstanceOf(Exception.class);
    }
}