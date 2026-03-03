package com.example.identityservice.service;

import com.example.identityservice.dto.OAuth2UserInfo;
import com.example.identityservice.entity.OAuth2Account;
import com.example.identityservice.entity.User;
import com.example.identityservice.entity.enums.UserRole;
import com.example.identityservice.entity.enums.UserStatus;
import com.example.identityservice.repository.OAuth2Repository;
import com.example.identityservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class OAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(OAuth2UserService.class);

    private final UserRepository userRepository;
    private final OAuth2Repository oAuth2Repository;

    public OAuth2UserService(
            UserRepository userRepository,
            OAuth2Repository oAuth2Repository
    ) {
        this.userRepository = userRepository;
        this.oAuth2Repository = oAuth2Repository;
    }

    @Transactional
    public User processOAuth2User(OAuth2UserInfo userInfo) {

        Optional<OAuth2Account> existingAccount =
                oAuth2Repository.findByProviderAndProviderId(
                        userInfo.provider(), userInfo.providerId()
                );

        if (existingAccount.isPresent()) {
            User user = existingAccount.get().getUser();
            user.setLastLoginAt(OffsetDateTime.now());
            updateAvatarIfEmpty(user, userInfo);
            log.info("OAuth2 login: existing link, user={}, provider={}",
                    user.getUsername(), userInfo.provider());
            return userRepository.save(user);
        }

        if (userInfo.email() != null) {
            Optional<User> existingUser = userRepository.findByEmail(userInfo.email());
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                linkOAuth2Account(user, userInfo);
                user.setLastLoginAt(OffsetDateTime.now());
                updateAvatarIfEmpty(user, userInfo);
                log.info("OAuth2 login: linked to existing user={}, provider={}",
                        user.getUsername(), userInfo.provider());
                return userRepository.save(user);
            }
        }

        User newUser = createNewUser(userInfo);
        log.info("OAuth2 login: created new user={}, provider={}",
                newUser.getUsername(), userInfo.provider());
        return newUser;
    }

    private User createNewUser(OAuth2UserInfo userInfo) {
        String username = generateUniqueUsername(userInfo.name());

        User user = User.builder()
                .username(username)
                .email(userInfo.email())
                .passwordHash(null)
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .avatarUrl(userInfo.avatarUrl())
                .build();

        user = userRepository.save(user);
        linkOAuth2Account(user, userInfo);

        return user;
    }

    private void linkOAuth2Account(User user, OAuth2UserInfo userInfo) {
        OAuth2Account account = OAuth2Account.builder()
                .user(user)
                .provider(userInfo.provider())
                .providerId(userInfo.providerId())
                .build();

        oAuth2Repository.save(account);
    }

    private void updateAvatarIfEmpty(User user, OAuth2UserInfo userInfo) {
        if (user.getAvatarUrl() == null && userInfo.avatarUrl() != null) {
            user.setAvatarUrl(userInfo.avatarUrl());
        }
    }

    private String generateUniqueUsername(String name) {
        if (name == null || name.isBlank()) {
            name = "user";
        }

        String base = name.toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        if (base.length() > 40) {
            base = base.substring(0, 40);
        }
        if (base.isEmpty()) {
            base = "user";
        }

        String candidate = base;
        while (userRepository.existsByUsername(candidate)) {
            String suffix = UUID.randomUUID().toString().substring(0, 4);
            candidate = base + "_" + suffix;
        }

        return candidate;
    }
}
