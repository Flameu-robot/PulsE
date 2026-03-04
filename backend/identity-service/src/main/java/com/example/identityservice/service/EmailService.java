package com.example.identityservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    // Убераю предупреждение - bean создаётся автоматически ide не видит...
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String token) {
        String subject = "Подтверждение email — Pulse";
        String text = "Ваш код подтверждения: " + token + "\n\n"
                + "Код действителен 15 минут.\n"
                + "Если вы не регистрировались — просто проигнорируйте это письмо.";

        send(to, subject, text);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Сброс пароля — Pulse";
        String text = "Ваш код для сброса пароля: " + token + "\n\n"
                + "Код действителен 15 минут.\n"
                + "Если вы не запрашивали сброс — просто проигнорируйте это письмо.";

        send(to, subject, text);
    }

    private void send(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("noreply@pulse.app");

            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
