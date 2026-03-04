package com.example.identityservice.service;

import com.example.identityservice.dto.response.AuthResponse;
import com.example.identityservice.entity.RefreshToken;
import com.example.identityservice.entity.User;
import com.example.identityservice.entity.enums.UserRole;
import com.example.identityservice.entity.enums.UserStatus;
import com.example.identityservice.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthResponseFactoryTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private HttpServletRequest httpRequest;

    private AuthResponseFactory factory;
    private User testUser;

    @BeforeEach
    void setUp() {
        factory = new AuthResponseFactory(jwtService, tokenRepository);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .passwordHash("hashed")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("should create auth response with tokens")
    void shouldCreateAuthResponse() {
        when(jwtService.generateAccessToken(1L, "testuser", "USER"))
                .thenReturn("access-token");
        when(jwtService.generateRefreshToken(1L, "testuser"))
                .thenReturn("refresh-token");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000000L);
        when(tokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(httpRequest.getHeader("User-Agent")).thenReturn("TestBrowser");
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        AuthResponse response = factory.create(testUser, httpRequest);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.role()).isEqualTo("USER");

        verify(tokenRepository).save(argThat(token -> {
            assertThat(token.getUser()).isEqualTo(testUser);
            assertThat(token.getTokenHash()).isNotBlank();
            assertThat(token.getUserAgent()).isEqualTo("TestBrowser");
            assertThat(token.getIpAddress()).isEqualTo("127.0.0.1");
            assertThat(token.isRevoked()).isFalse();
            return true;
        }));
    }

    @Test
    @DisplayName("should handle null httpRequest gracefully")
    void shouldHandleNullHttpRequest() {
        when(jwtService.generateAccessToken(1L, "testuser", "USER"))
                .thenReturn("access-token");
        when(jwtService.generateRefreshToken(1L, "testuser"))
                .thenReturn("refresh-token");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000000L);
        when(tokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = factory.create(testUser, null);

        assertThat(response.accessToken()).isEqualTo("access-token");

        verify(tokenRepository).save(argThat(token -> {
            assertThat(token.getUserAgent()).isNull();
            assertThat(token.getIpAddress()).isNull();
            return true;
        }));
    }

    @Test
    @DisplayName("should produce consistent hash for same token")
    void shouldHashConsistently() {
        String hash1 = factory.hashToken("my-token");
        String hash2 = factory.hashToken("my-token");

        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).hasSize(64);
    }

    @Test
    @DisplayName("should produce different hash for different tokens")
    void shouldHashDifferently() {
        String hash1 = factory.hashToken("token-a");
        String hash2 = factory.hashToken("token-b");

        assertThat(hash1).isNotEqualTo(hash2);
    }
}
