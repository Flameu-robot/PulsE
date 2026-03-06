package com.example.identityservice.service;

import com.example.identityservice.dto.request.ChangePasswordRequest;
import com.example.identityservice.dto.request.LoginRequest;
import com.example.identityservice.dto.request.RefreshTokenRequest;
import com.example.identityservice.dto.request.RegisterRequest;
import com.example.identityservice.dto.response.AuthResponse;
import com.example.identityservice.dto.response.UserResponse;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
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
    private AuthResponseFactory authResponseFactory;

    @Mock
    private HttpServletRequest httpRequest;

    private AuthService authService;

    private User testUser;
    private AuthResponse defaultAuthResponse;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                refreshTokenRepository,
                jwtService,
                passwordEncoder,
                authenticationManager,
                authResponseFactory
        );

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .passwordHash("$2a$10$hashedpassword")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        defaultAuthResponse = new AuthResponse(
                "access-token",
                "refresh-token",
                1L,
                "testuser",
                "USER"
        );
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
            when(authResponseFactory.create(any(User.class), any(HttpServletRequest.class)))
                    .thenReturn(defaultAuthResponse);

            AuthResponse response = authService.register(validRequest, httpRequest);

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            assertThat(response.userId()).isEqualTo(1L);
            assertThat(response.username()).isEqualTo("testuser");
            assertThat(response.role()).isEqualTo("USER");

            verify(userRepository).save(any(User.class));
            verify(authResponseFactory).create(any(User.class), eq(httpRequest));
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

        @Test
        @DisplayName("should login with username successfully")
        void shouldLoginWithUsername() {
            LoginRequest request = new LoginRequest("testuser", "password123");

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(new UsernamePasswordAuthenticationToken("testuser", null));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(authResponseFactory.create(any(User.class), any(HttpServletRequest.class)))
                    .thenReturn(defaultAuthResponse);

            AuthResponse response = authService.login(request, httpRequest);

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.username()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should login with email successfully")
        void shouldLoginWithEmail() {
            LoginRequest request = new LoginRequest("test@test.com", "password123");

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(new UsernamePasswordAuthenticationToken("testuser", null));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(authResponseFactory.create(any(User.class), any(HttpServletRequest.class)))
                    .thenReturn(defaultAuthResponse);

            AuthResponse response = authService.login(request, httpRequest);

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.username()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should throw on bad credentials")
        void shouldThrowOnBadCredentials() {
            LoginRequest request = new LoginRequest("testuser", "wrongpassword");

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(request, httpRequest))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("should throw when user not found by username")
        void shouldThrowWhenUsernameNotFound() {
            LoginRequest request = new LoginRequest("unknown", "password123");

            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request, httpRequest))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("should throw when user not found by email")
        void shouldThrowWhenEmailNotFound() {
            LoginRequest request = new LoginRequest("unknown@test.com", "password123");

            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request, httpRequest))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("should revoke specific token on logout")
        void shouldRevokeSpecificToken() {
            String rawToken = "valid-refresh-token";
            String tokenHash = "hashed-token";

            RefreshToken storedToken = RefreshToken.builder()
                    .id(1L)
                    .user(testUser)
                    .tokenHash(tokenHash)
                    .revoked(false)
                    .expiresAt(OffsetDateTime.now().plusDays(1))
                    .build();

            when(authResponseFactory.hashToken(rawToken)).thenReturn(tokenHash);
            when(refreshTokenRepository.findByTokenHash(tokenHash))
                    .thenReturn(Optional.of(storedToken));

            authService.logout("testuser", rawToken);

            verify(refreshTokenRepository).save(argThat(token -> token.isRevoked()));
        }

        @Test
        @DisplayName("should throw when token not found")
        void shouldThrowWhenTokenNotFound() {
            when(authResponseFactory.hashToken("unknown-token")).thenReturn("unknown-hash");
            when(refreshTokenRepository.findByTokenHash("unknown-hash"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.logout("testuser", "unknown-token"))
                    .isInstanceOf(TokenException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("should throw when token belongs to another user")
        void shouldThrowWhenTokenBelongsToAnotherUser() {
            User otherUser = User.builder()
                    .id(2L).username("otheruser").build();

            RefreshToken storedToken = RefreshToken.builder()
                    .id(1L)
                    .user(otherUser)
                    .tokenHash("hashed")
                    .revoked(false)
                    .expiresAt(OffsetDateTime.now().plusDays(1))
                    .build();

            when(authResponseFactory.hashToken("some-token")).thenReturn("hashed");
            when(refreshTokenRepository.findByTokenHash("hashed"))
                    .thenReturn(Optional.of(storedToken));

            assertThatThrownBy(() -> authService.logout("testuser", "some-token"))
                    .isInstanceOf(TokenException.class)
                    .hasMessageContaining("does not belong");
        }
    }

    @Nested
    @DisplayName("logoutAll")
    class LogoutAll {

        @Test
        @DisplayName("should revoke all tokens")
        void shouldRevokeAllTokens() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            authService.logoutAll("testuser");

            verify(refreshTokenRepository).revokeAllByUserId(1L);
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.logoutAll("unknown"))
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

            assertThatThrownBy(() -> authService.refresh(request, httpRequest))
                    .isInstanceOf(TokenException.class)
                    .hasMessageContaining("Invalid");
        }

        @Test
        @DisplayName("should throw when token type is not refresh")
        void shouldThrowWhenNotRefreshType() {
            RefreshTokenRequest request = new RefreshTokenRequest("access-token");
            when(jwtService.isTokenValid("access-token")).thenReturn(true);
            when(jwtService.extractTokenType("access-token")).thenReturn("access");

            assertThatThrownBy(() -> authService.refresh(request, httpRequest))
                    .isInstanceOf(TokenException.class)
                    .hasMessageContaining("not a refresh");
        }

        @Test
        @DisplayName("should refresh token successfully")
        void shouldRefreshSuccessfully() {
            RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
            String tokenHash = "hashed-token";

            RefreshToken storedToken = RefreshToken.builder()
                    .id(1L)
                    .user(testUser)
                    .tokenHash(tokenHash)
                    .revoked(false)
                    .expiresAt(OffsetDateTime.now().plusDays(1))
                    .build();

            when(jwtService.isTokenValid("valid-refresh-token")).thenReturn(true);
            when(jwtService.extractTokenType("valid-refresh-token")).thenReturn("refresh");
            when(authResponseFactory.hashToken("valid-refresh-token")).thenReturn(tokenHash);
            when(refreshTokenRepository.findByTokenHash(tokenHash))
                    .thenReturn(Optional.of(storedToken));
            when(authResponseFactory.create(eq(testUser), eq(httpRequest)))
                    .thenReturn(defaultAuthResponse);

            AuthResponse response = authService.refresh(request, httpRequest);

            assertThat(response.accessToken()).isEqualTo("access-token");
            verify(refreshTokenRepository).save(argThat(RefreshToken::isRevoked));
        }
    }

    @Nested
    @DisplayName("getCurrentUser")
    class GetCurrentUser {

        @Test
        @DisplayName("should return user response")
        void shouldReturnUserResponse() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            UserResponse response = authService.getCurrentUser("testuser");

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.username()).isEqualTo("testuser");
            assertThat(response.email()).isEqualTo("test@test.com");
            assertThat(response.role()).isEqualTo("USER");
            assertThat(response.status()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.getCurrentUser("unknown"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        private ChangePasswordRequest validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new ChangePasswordRequest("oldPassword123", "newPassword123");
        }

        @Test
        @DisplayName("should change password successfully")
        void shouldChangePasswordSuccessfully() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("oldPassword123", "$2a$10$hashedpassword")).thenReturn(true);
            when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$newhashedpassword");

            authService.changePassword("testuser", validRequest);

            verify(userRepository).save(argThat(user ->
                    user.getPasswordHash().equals("$2a$10$newhashedpassword")
            ));
            verify(refreshTokenRepository).revokeAllByUserId(1L);
        }

        @Test
        @DisplayName("should throw when current password is wrong")
        void shouldThrowWhenCurrentPasswordWrong() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("oldPassword123", "$2a$10$hashedpassword")).thenReturn(false);

            assertThatThrownBy(() -> authService.changePassword("testuser", validRequest))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when user has no password (OAuth user)")
        void shouldThrowWhenUserHasNoPassword() {
            User oauthUser = User.builder()
                    .id(2L)
                    .username("oauthuser")
                    .passwordHash(null)
                    .build();

            when(userRepository.findByUsername("oauthuser")).thenReturn(Optional.of(oauthUser));

            assertThatThrownBy(() -> authService.changePassword("oauthuser", validRequest))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.changePassword("unknown", validRequest))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
