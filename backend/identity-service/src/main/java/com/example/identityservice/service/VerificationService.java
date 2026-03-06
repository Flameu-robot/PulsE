package com.example.identityservice.service;

import com.example.identityservice.entity.User;
import com.example.identityservice.entity.VerificationToken;
import com.example.identityservice.entity.enums.UserStatus;
import com.example.identityservice.entity.enums.VerificationType;
import com.example.identityservice.repository.UserRepository;
import com.example.identityservice.repository.VerificationRepository;
import exception.auth.TokenException;
import exception.auth.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;

@Service
public class VerificationService {

    private static final Logger log = LoggerFactory.getLogger(VerificationService.class);
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 15;

    private final VerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public VerificationService(
            VerificationRepository verificationRepository,
            UserRepository userRepository,
            EmailService emailService
    ) {
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void sendVerificationCode(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        if (user.getStatus() == UserStatus.ACTIVE) {
            log.info("User {} already verified", username);
            return;
        }

        if (user.getEmail() == null) {
            throw new RuntimeException("User has no email");
        }

        String code = generateCode();

        verificationRepository.findByUserIdAndType(user.getId(), VerificationType.EMAIL_VERIFY)
                .ifPresent(existing -> {
                    existing.setUsed(true);
                    verificationRepository.save(existing);
                });

        VerificationToken token = VerificationToken.builder()
                .user(user)
                .token(code)
                .type(VerificationType.EMAIL_VERIFY)
                .expiresAt(OffsetDateTime.now().plusMinutes(EXPIRATION_MINUTES))
                .build();

        verificationRepository.save(token);

        emailService.sendVerificationEmail(user.getEmail(), code);
        log.info("Verification code sent to user {}", username);
    }

    @Transactional
    public void verifyEmail(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        VerificationToken token = verificationRepository.findByToken(code)
                .orElseThrow(() -> new TokenException("Invalid verification code"));

        if (!token.getUser().getId().equals(user.getId())) {
            throw new TokenException("Invalid verification code");
        }

        if (token.getType() != VerificationType.EMAIL_VERIFY) {
            throw new TokenException("Invalid token type");
        }

        if (!token.isValid()) {
            throw new TokenException("Verification code expired or already used");
        }

        token.setUsed(true);
        verificationRepository.save(token);

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("Email verified for user {}", username);
    }

    @Transactional
    public void sendPasswordResetCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            log.info("Password reset requested for non-existent email: {}", email);
            return;
        }

        String code = generateCode();

        verificationRepository.findByUserIdAndType(user.getId(), VerificationType.PASSWORD_RESET)
                .ifPresent(existing -> {
                    existing.setUsed(true);
                    verificationRepository.save(existing);
                });

        VerificationToken token = VerificationToken.builder()
                .user(user)
                .token(code)
                .type(VerificationType.PASSWORD_RESET)
                .expiresAt(OffsetDateTime.now().plusMinutes(EXPIRATION_MINUTES))
                .build();

        verificationRepository.save(token);

        emailService.sendPasswordResetEmail(user.getEmail(), code);
        log.info("Password reset code sent to {}", email);
    }

    @Transactional
    public void resetPassword(String email, String code, String newPasswordHash) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new TokenException("Invalid reset request"));

        VerificationToken token = verificationRepository.findByToken(code)
                .orElseThrow(() -> new TokenException("Invalid reset code"));

        if (!token.getUser().getId().equals(user.getId())) {
            throw new TokenException("Invalid reset code");
        }

        if (token.getType() != VerificationType.PASSWORD_RESET) {
            throw new TokenException("Invalid token type");
        }

        if (!token.isValid()) {
            throw new TokenException("Reset code expired or already used");
        }

        token.setUsed(true);
        verificationRepository.save(token);

        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);

        log.info("Password reset for user {}", user.getUsername());
    }

    private String generateCode() {
        int code = secureRandom.nextInt(900_000) + 100_000;
        return String.valueOf(code);
    }
}
