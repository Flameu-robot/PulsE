package com.example.identityservice.service;

import com.example.identityservice.dto.response.OAuth2AccountResponse;
import com.example.identityservice.entity.User;
import com.example.identityservice.entity.enums.OAuthProvider;
import com.example.identityservice.repository.OAuth2Repository;
import com.example.identityservice.repository.UserRepository;
import com.example.identityservice.repository.WebAuthRepository;
import exception.auth.TokenException;
import exception.auth.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OAuth2AccountService {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AccountService.class);

    private final OAuth2Repository oAuth2Repository;
    private final UserRepository userRepository;
    private final WebAuthRepository webAuthRepository;

    public OAuth2AccountService(
            OAuth2Repository oAuth2Repository,
            UserRepository userRepository,
            WebAuthRepository webAuthRepository
    ) {
        this.oAuth2Repository = oAuth2Repository;
        this.userRepository = userRepository;
        this.webAuthRepository = webAuthRepository;
    }

    public List<OAuth2AccountResponse> getLinkedAccounts(String username) {
        User user = findUser(username);

        return oAuth2Repository.findAllByUserId(user.getId()).stream()
                .map(a -> new OAuth2AccountResponse(
                        a.getId(),
                        a.getProvider().name(),
                        a.getProviderId(),
                        a.getLinkedAt()
                ))
                .toList();
    }

    @Transactional
    public void unlinkAccount(String username, String provider) {
        User user = findUser(username);

        OAuthProvider oauthProvider;
        try {
            oauthProvider = OAuthProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new TokenException("Unknown OAuth2 provider: " + provider);
        }

        long linkedCount = oAuth2Repository.countByUserId(user.getId());
        if (linkedCount == 0) {
            throw new TokenException("No OAuth2 accounts linked");
        }

        boolean hasPassword = user.getPasswordHash() != null;
        long webAuthnCount = webAuthRepository.findAllByUserId(user.getId()).size();

        if (!hasPassword && linkedCount <= 1 && webAuthnCount == 0) {
            throw new TokenException(
                    "Cannot unlink the only authentication method. " +
                            "Set a password or register a security key first."
            );
        }

        oAuth2Repository.deleteByUserIdAndProvider(user.getId(), oauthProvider);
        log.info("OAuth2 provider {} unlinked for user {}", provider, username);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }
}
