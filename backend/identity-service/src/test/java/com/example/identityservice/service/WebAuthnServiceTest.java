package com.example.identityservice.service;

import com.example.identityservice.entity.User;
import com.example.identityservice.entity.WebAuthnCredential;
import com.example.identityservice.entity.enums.UserRole;
import com.example.identityservice.entity.enums.UserStatus;
import com.example.identityservice.repository.UserRepository;
import com.example.identityservice.repository.WebAuthRepository;
import com.yubico.webauthn.RelyingParty;
import exception.auth.TokenException;
import exception.auth.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebAuthnServiceTest {

    @Mock
    private RelyingParty relyingParty;

    @Mock
    private WebAuthnChallengeStore challengeStore;

    @Mock
    private WebAuthRepository webAuthRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthResponseFactory authResponseFactory;

    private WebAuthnService webAuthnService;

    private User testUser;

    @BeforeEach
    void setUp() {
        webAuthnService = new WebAuthnService(
                relyingParty,
                challengeStore,
                webAuthRepository,
                userRepository,
                authResponseFactory
        );

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .passwordHash("hashed")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();
    }

    @Nested
    @DisplayName("startRegistration")
    class StartRegistration {

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByUsername("unknown"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> webAuthnService.startRegistration("unknown"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("finishRegistration")
    class FinishRegistration {

        @Test
        @DisplayName("should throw when flow expired")
        void shouldThrowWhenFlowExpired() {
            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(testUser));
            when(challengeStore.getAndRemove("expired-flow"))
                    .thenThrow(new RuntimeException("WebAuthn flow expired or not found"));

            assertThatThrownBy(() ->
                    webAuthnService.finishRegistration(
                            "testuser", "expired-flow", "{}", "My Key"
                    ))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("expired");
        }
    }

    @Nested
    @DisplayName("removeCredential")
    class RemoveCredential {

        @Test
        @DisplayName("should remove credential successfully")
        void shouldRemoveCredential() {
            WebAuthnCredential credential = WebAuthnCredential.builder()
                    .id(10L)
                    .user(testUser)
                    .credentialId(new byte[]{1, 2, 3})
                    .publicKey(new byte[]{4, 5, 6})
                    .build();

            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(testUser));
            when(webAuthRepository.findById(10L))
                    .thenReturn(Optional.of(credential));
            // Два ключа — можно удалить один
            when(webAuthRepository.findAllByUserId(1L))
                    .thenReturn(List.of(credential, credential));

            webAuthnService.removeCredential("testuser", 10L);

            verify(webAuthRepository).delete(credential);
        }

        @Test
        @DisplayName("should throw when credential not found")
        void shouldThrowWhenCredentialNotFound() {
            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(testUser));
            when(webAuthRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    webAuthnService.removeCredential("testuser", 999L))
                    .isInstanceOf(TokenException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("should throw when credential belongs to another user")
        void shouldThrowWhenNotOwner() {
            User otherUser = User.builder()
                    .id(2L).username("other").build();

            WebAuthnCredential credential = WebAuthnCredential.builder()
                    .id(10L)
                    .user(otherUser)
                    .credentialId(new byte[]{1, 2, 3})
                    .publicKey(new byte[]{4, 5, 6})
                    .build();

            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(testUser));
            when(webAuthRepository.findById(10L))
                    .thenReturn(Optional.of(credential));

            assertThatThrownBy(() ->
                    webAuthnService.removeCredential("testuser", 10L))
                    .isInstanceOf(TokenException.class)
                    .hasMessageContaining("does not belong");
        }

        @Test
        @DisplayName("should throw when removing last auth method without password")
        void shouldThrowWhenRemovingLastMethod() {
            User noPasswordUser = User.builder()
                    .id(1L)
                    .username("testuser")
                    .passwordHash(null)
                    .build();

            WebAuthnCredential credential = WebAuthnCredential.builder()
                    .id(10L)
                    .user(noPasswordUser)
                    .credentialId(new byte[]{1, 2, 3})
                    .publicKey(new byte[]{4, 5, 6})
                    .build();

            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(noPasswordUser));
            when(webAuthRepository.findById(10L))
                    .thenReturn(Optional.of(credential));
            when(webAuthRepository.findAllByUserId(1L))
                    .thenReturn(List.of(credential));

            assertThatThrownBy(() ->
                    webAuthnService.removeCredential("testuser", 10L))
                    .isInstanceOf(TokenException.class)
                    .hasMessageContaining("Cannot remove");
        }

        @Test
        @DisplayName("should allow removing key when user has password")
        void shouldAllowRemovingWhenHasPassword() {
            WebAuthnCredential credential = WebAuthnCredential.builder()
                    .id(10L)
                    .user(testUser)
                    .credentialId(new byte[]{1, 2, 3})
                    .publicKey(new byte[]{4, 5, 6})
                    .build();

            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(testUser));
            when(webAuthRepository.findById(10L))
                    .thenReturn(Optional.of(credential));
            when(webAuthRepository.findAllByUserId(1L))
                    .thenReturn(List.of(credential));

            webAuthnService.removeCredential("testuser", 10L);

            verify(webAuthRepository).delete(credential);
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByUsername("unknown"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    webAuthnService.removeCredential("unknown", 10L))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}