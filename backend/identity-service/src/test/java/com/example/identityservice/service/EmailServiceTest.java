package com.example.identityservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    @Test
    @DisplayName("should send verification email")
    void shouldSendVerificationEmail() {
        emailService.sendVerificationEmail("user@test.com", "123456");

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();

        assertThat(message.getTo()).contains("user@test.com");
        assertThat(message.getSubject()).contains("Подтверждение");
        assertThat(message.getText()).contains("123456");
    }

    @Test
    @DisplayName("should send password reset email")
    void shouldSendPasswordResetEmail() {
        emailService.sendPasswordResetEmail("user@test.com", "654321");

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();

        assertThat(message.getTo()).contains("user@test.com");
        assertThat(message.getSubject()).contains("Сброс пароля");
        assertThat(message.getText()).contains("654321");
    }

    @Test
    @DisplayName("should throw when mail sending fails")
    void shouldThrowWhenMailFails() {
        doThrow(new RuntimeException("SMTP error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() ->
                emailService.sendVerificationEmail("user@test.com", "123456"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send email");
    }
}
