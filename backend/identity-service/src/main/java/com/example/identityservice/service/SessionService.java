package com.example.identityservice.service;

import com.example.identityservice.dto.response.SessionResponse;
import com.example.identityservice.entity.RefreshToken;
import com.example.identityservice.entity.User;
import com.example.identityservice.repository.TokenRepository;
import com.example.identityservice.repository.UserRepository;
import exception.auth.TokenException;
import exception.auth.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    public SessionService(TokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    public List<SessionResponse> getActiveSessions(String username) {
        User user = findUser(username);

        return tokenRepository.findActiveByUserId(user.getId()).stream()
                .map(this::toSessionResponse)
                .toList();
    }

    @Transactional
    public void revokeSession(String username, Long tokenId) {
        User user = findUser(username);

        RefreshToken token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new TokenException("Session not found"));

        if (!token.getUser().getId().equals(user.getId())) {
            throw new TokenException("Session does not belong to user");
        }

        if (token.isRevoked()) {
            throw new TokenException("Session already revoked");
        }

        token.setRevoked(true);
        tokenRepository.save(token);
        log.info("Session {} revoked for user {}", tokenId, username);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    private SessionResponse toSessionResponse(RefreshToken token) {
        return new SessionResponse(
                token.getId(),
                token.getUserAgent(),
                token.getIpAddress(),
                token.getCreatedAt(),
                token.getExpiresAt()
        );
    }
}
