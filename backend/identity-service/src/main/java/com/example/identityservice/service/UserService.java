package com.example.identityservice.service;

import com.example.identityservice.dto.request.UpdateProfileRequest;
import com.example.identityservice.dto.response.PublicUserResponse;
import com.example.identityservice.dto.response.UserResponse;
import com.example.identityservice.entity.User;
import com.example.identityservice.repository.TokenRepository;
import com.example.identityservice.repository.UserRepository;
import com.example.identityservice.repository.VerificationRepository;
import exception.auth.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final VerificationRepository verificationRepository;

    public UserService(
            UserRepository userRepository,
            TokenRepository tokenRepository,
            VerificationRepository verificationRepository
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.verificationRepository = verificationRepository;
    }

    public UserResponse getProfile(String username) {
        User user = findByUsername(username);
        return toUserResponse(user);
    }

    public PublicUserResponse getPublicProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        return new PublicUserResponse(
                user.getId(),
                user.getUsername(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getCreatedAt()
        );
    }

    @Transactional
    public UserResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = findByUsername(username);

        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }

        user = userRepository.save(user);
        log.info("Profile updated for user {}", username);

        return toUserResponse(user);
    }


    @Transactional
    public void deleteAccount(String username) {
        User user = findByUsername(username);

        tokenRepository.deleteAllByUserId(user.getId());
        verificationRepository.deleteAllByUserId(user.getId());

        userRepository.delete(user);
        log.info("Account deleted for user {}", username);
    }

    private User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}
