package com.example.identityservice.service;

import com.example.identityservice.entity.User;
import com.example.identityservice.entity.VerificationToken;
import com.example.identityservice.entity.enums.UserStatus;
import com.example.identityservice.entity.enums.VerificationType;
import com.example.identityservice.repository.UserRepository;
import com.example.identityservice.repository.VerificationRepository;
import exception.auth.TokenException;
import exception.auth.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private VerificationRepository verificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private VerificationService verificationService;

    @Captor
    private ArgumentCaptor<VerificationToken> tokenCaptor;

    private User pendingUser;
    private User activeUser;

    @BeforeEach
    void setUp() {
        pendingUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .status(UserStatus.PENDING)
                .build();

        activeUser = User.builder()
                .id(2L)
                .username("activeuser")
                .email("active@test.com")
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("sendVerificationCode")
    class SendVerificationCode {

        @Test
        @DisplayName("should send code to pending user")
        void shouldSendCodeToPendingUser() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(pendingUser));
            when(verificationRepository.findByUserIdAndType(1L, VerificationType.EMAIL_VERIFY))
                    .thenReturn(Optional.empty());
            when(verificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            verificationService.sendVerificationCode("testuser");

            verify(verificationRepository).save(tokenCaptor.capture());
            VerificationToken saved = tokenCaptor.getValue();
            assertThat(saved.getType()).isEqualTo(VerificationType.EMAIL_VERIFY);
            assertThat(saved.getToken()).hasSize(6);
            assertThat(saved.getUser().getId()).isEqualTo(1L);
            assertThat(saved.getExpiresAt()).isAfter(OffsetDateTime.now());

            verify(emailService).sendVerificationEmail(eq("test@test.com"), any());
        }

        @Test
        @DisplayName("should skip if user already active")
        void shouldSkipIfAlreadyActive() {
            when(userRepository.findByUsername("activeuser")).thenReturn(Optional.of(activeUser));

            verificationService.sendVerificationCode("activeuser");

            verify(verificationRepository, never()).save(any());
            verify(emailService, never()).sendVerificationEmail(any(), any());
        }

        @Test
        @DisplayName("should invalidate old token before sending new")
        void shouldInvalidateOldToken() {
            VerificationToken oldToken = VerificationToken.builder()
                    .id(10L)
                    .user(pendingUser)
                    .token("123456")
                    .type(VerificationType.EMAIL_VERIFY)
                    .used(false)
                    .build();

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(pendingUser));
            when(verificationRepository.findByUserIdAndType(1L, VerificationType.EMAIL_VERIFY))
                    .thenReturn(Optional.of(oldToken));
            when(verificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            verificationService.sendVerificationCode("testuser");

            // Старый токен помечен как использованный
            assertThat(oldToken.isUsed()).isTrue();
            // save вызван дважды: для старого и для нового
            verify(verificationRepository, times(2)).save(any());
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> verificationService.sendVerificationCode("unknown"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("verifyEmail")
    class VerifyEmail {

        @Test
        @DisplayName("should verify email successfully")
        void shouldVerifyEmailSuccessfully() {
            VerificationToken token = VerificationToken.builder()
                    .id(1L)
                    .user(pendingUser)
                    .token("123456")
                    .type(VerificationType.EMAIL_VERIFY)
                    .expiresAt(OffsetDateTime.now().plusMinutes(10))
                    .used(false)
                    .build();

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(pendingUser));
            when(verificationRepository.findByToken("123456")).thenReturn(Optional.of(token));

            verificationService.verifyEmail("testuser", "123456");

            assertThat(token.isUsed()).isTrue();
            assertThat(pendingUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
            verify(verificationRepository).save(token);
            verify(userRepository).save(pendingUser);
        }

        @Test
        @DisplayName("should throw on invalid code")
        void shouldThrowOnInvalidCode() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(pendingUser));
            when(verificationRepository.findByToken("999999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> verificationService.verifyEmail("testuser", "999999"))
                    .isInstanceOf(TokenException.class)
                    .hasMessageContaining("Invalid");
        }

        @Test
        @DisplayName("should throw on expired code")
        void shouldThrowOnExpiredCode() {
            VerificationToken token = VerificationToken.builder()
                    .id(1L)
                    .user(pendingUser)
                    .token("123456")
                    .type(VerificationType.EMAIL_VERIFY)
                    .expiresAt(OffsetDateTime.now().minusMinutes(1))
                    .used(false)
                    .build();

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(pendingUser));
            when(verificationRepository.findByToken("123456")).thenReturn(Optional.of(token));

            assertThatThrownBy(() -> verificationService.verifyEmail("testuser", "123456"))
                    .isInstanceOf(TokenException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        @DisplayName("should throw when code belongs to different user")
        void shouldThrowWhenCodeBelongsToDifferentUser() {
            VerificationToken token = VerificationToken.builder()
                    .id(1L)
                    .user(activeUser) // другой пользователь
                    .token("123456")
                    .type(VerificationType.EMAIL_VERIFY)
                    .expiresAt(OffsetDateTime.now().plusMinutes(10))
                    .used(false)
                    .build();

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(pendingUser));
            when(verificationRepository.findByToken("123456")).thenReturn(Optional.of(token));

            assertThatThrownBy(() -> verificationService.verifyEmail("testuser", "123456"))
                    .isInstanceOf(TokenException.class);
        }
    }

    @Nested
    @DisplayName("sendPasswordResetCode")
    class SendPasswordResetCode {

        @Test
        @DisplayName("should send reset code")
        void shouldSendResetCode() {
            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(pendingUser));
            when(verificationRepository.findByUserIdAndType(1L, VerificationType.PASSWORD_RESET))
                    .thenReturn(Optional.empty());
            when(verificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            verificationService.sendPasswordResetCode("test@test.com");

            verify(verificationRepository).save(tokenCaptor.capture());
            assertThat(tokenCaptor.getValue().getType()).isEqualTo(VerificationType.PASSWORD_RESET);

            verify(emailService).sendPasswordResetEmail(eq("test@test.com"), any());
        }

        @Test
        @DisplayName("should not reveal if email does not exist")
        void shouldNotRevealNonExistentEmail() {
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            // Не бросает исключение
            verificationService.sendPasswordResetCode("unknown@test.com");

            verify(verificationRepository, never()).save(any());
            verify(emailService, never()).sendPasswordResetEmail(any(), any());
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class ResetPassword {

        @Test
        @DisplayName("should reset password successfully")
        void shouldResetPasswordSuccessfully() {
            VerificationToken token = VerificationToken.builder()
                    .id(1L)
                    .user(pendingUser)
                    .token("654321")
                    .type(VerificationType.PASSWORD_RESET)
                    .expiresAt(OffsetDateTime.now().plusMinutes(10))
                    .used(false)
                    .build();

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(pendingUser));
            when(verificationRepository.findByToken("654321")).thenReturn(Optional.of(token));

            verificationService.resetPassword("test@test.com", "654321", "new-hashed-password");

            assertThat(token.isUsed()).isTrue();
            assertThat(pendingUser.getPasswordHash()).isEqualTo("new-hashed-password");
            verify(verificationRepository).save(token);
            verify(userRepository).save(pendingUser);
        }

        @Test
        @DisplayName("should throw on invalid reset code")
        void shouldThrowOnInvalidCode() {
            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(pendingUser));
            when(verificationRepository.findByToken("000000")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    verificationService.resetPassword("test@test.com", "000000", "hash"))
                    .isInstanceOf(TokenException.class);
        }
    }
}
