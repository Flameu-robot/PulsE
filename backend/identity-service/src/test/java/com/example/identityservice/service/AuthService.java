package com.example.identityservice.service;

import com.example.identityservice.dto.request.LoginRequest;
import com.example.identityservice.dto.request.RefreshTokenRequest;
import com.example.identityservice.dto.request.RegisterRequest;
import com.example.identityservice.dto.response.AuthResponse;
import com.example.identityservice.entity.RefreshToken;
import com.example.identityservice.entity.User;
import com.example.identityservice.entity.enums.UserRole;
import com.example.identityservice.entity.enums.UserStatus;
import com.example.identityservice.repository.TokenRepository;
import com.example.identityservice.repository.UserRepository;
import exception.auth.InvalidCredentialsException;
import exception.auth.TokenException;
import exception.auth.UserAlreadyExistsException;
import exception.auth.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .passwordHash("$2a$10$hashedpassword")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();
    }

    @Nested
    @DisplayName("register")
    class Register {

        private RegisterRequest validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new RegisterRequest("testuser", "test@test.com", "password123");
        }

        @Test
        @DisplayName("should register user successfully")
        void shouldRegisterSuccessfully() {
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedpassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateAccessToken(1L, "testuser", "USER")).thenReturn("access-token");
            when(jwtService.generateRefreshToken(1L, "testuser")).thenReturn("refresh-token");
            when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(new RefreshToken());
            when(httpRequest.getHeader("User-Agent")).thenReturn("TestBrowser");
            when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

            AuthResponse response = authService.register(validRequest, httpRequest);

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            assertThat(response.userId()).isEqualTo(1L);
            assertThat(response.username()).isEqualTo("testuser");
            assertThat(response.role()).isEqualTo("USER");

            verify(userRepository).save(any(User.class));
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("should throw when username already exists")
        void shouldThrowWhenUsernameExists() {
            when(userRepository.existsByUsername("testuser")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(validRequest, httpRequest))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("testuser");
        }

        @Test
        @DisplayName("should throw when email already exists")
        void shouldThrowWhenEmailExists() {
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(validRequest, httpRequest))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("test@test.com");
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        private LoginRequest validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new LoginRequest("testuser", "password123");
        }

        @Test
        @DisplayName("should login successfully")
        void shouldLoginSuccessfully() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(new UsernamePasswordAuthenticationToken("testuser", null));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateAccessToken(1L, "testuser", "USER")).thenReturn("access-token");
            when(jwtService.generateRefreshToken(1L, "testuser")).thenReturn("refresh-token");
            when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(new RefreshToken());
            when(httpRequest.getHeader("User-Agent")).thenReturn("TestBrowser");
            when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

            AuthResponse response = authService.login(validRequest, httpRequest);

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.username()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should throw on bad credentials")
        void shouldThrowOnBadCredentials() {
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(validRequest, httpRequest))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("should throw when user not found after auth")
        void shouldThrowWhenUserNotFound() {
            when(authenticationManager.authenticate(any()))
                    .thenReturn(new UsernamePasswordAuthenticationToken("testuser", null));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(validRequest, httpRequest))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("should revoke all tokens on logout")
        void shouldRevokeAllTokens() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            authService.logout("testuser");

            verify(refreshTokenRepository).revokeAllByUserId(1L);
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.logout("unknown"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("refresh")
    class Refresh {

        @Test
        @DisplayName("should throw on invalid token")
        void shouldThrowOnInvalidToken() {
            RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");
            when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(TokenException.class)
                    .hasMessageContaining("Invalid");
        }

        @Test
        @DisplayName("should throw when token type is not refresh")
        void shouldThrowWhenNotRefreshType() {
            RefreshTokenRequest request = new RefreshTokenRequest("access-token");
            when(jwtService.isTokenValid("access-token")).thenReturn(true);
            when(jwtService.extractTokenType("access-token")).thenReturn("access");

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(TokenException.class)
                    .hasMessageContaining("not a refresh");
        }
    }
}
