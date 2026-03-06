package com.example.identityservice.scheduler;

import com.example.identityservice.repository.TokenRepository;
import com.example.identityservice.repository.VerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TokenCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupScheduler.class);

    private final TokenRepository tokenRepository;
    private final VerificationRepository verificationRepository;

    public TokenCleanupScheduler(
            TokenRepository tokenRepository,
            VerificationRepository verificationRepository
    ) {
        this.tokenRepository = tokenRepository;
        this.verificationRepository = verificationRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupRefreshTokens() {
        int deleted = tokenRepository.deleteExpiredAndRevoked();
        if (deleted > 0) {
            log.info("Cleaned up {} expired/revoked refresh tokens", deleted);
        }
    }

    @Scheduled(cron = "0 30 * * * *")
    @Transactional
    public void cleanupVerificationTokens() {
        int deleted = verificationRepository.deleteExpiredAndUsed();
        if (deleted > 0) {
            log.info("Cleaned up {} expired/used verification tokens", deleted);
        }
    }
}
