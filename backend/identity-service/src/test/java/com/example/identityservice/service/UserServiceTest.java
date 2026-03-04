package com.example.identityservice.service;

import com.example.identityservice.dto.request.UpdateProfileRequest;
import com.example.identityservice.dto.response.PublicUserResponse;
import com.example.identityservice.dto.response.UserResponse;
import com.example.identityservice.entity.User;
import com.example.identityservice.entity.enums.UserRole;
import com.example.identityservice.entity.enums.UserStatus;
import com.example.identityservice.repository.TokenRepository;
import com.example.identityservice.repository.UserRepository;
import exception.auth.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .phone("+71234567890")
                .passwordHash("$2a$10$hash")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .avatarUrl("https://example.com/avatar.jpg")
                .bio("Hello world")
                .build();
    }

    @Nested
    @DisplayName("getProfile")
    class GetProfile {

        @Test
        @DisplayName("should return user profile")
        void shouldReturnProfile() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            UserResponse response = userService.getProfile("testuser");

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.username()).isEqualTo("testuser");
            assertThat(response.email()).isEqualTo("test@test.com");
            assertThat(response.phone()).isEqualTo("+71234567890");
            assertThat(response.role()).isEqualTo("USER");
            assertThat(response.status()).isEqualTo("ACTIVE");
            assertThat(response.avatarUrl()).isEqualTo("https://example.com/avatar.jpg");
            assertThat(response.bio()).isEqualTo("Hello world");
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenNotFound() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getProfile("unknown"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getPublicProfile")
    class GetPublicProfile {

        @Test
        @DisplayName("should return public profile")
        void shouldReturnPublicProfile() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            PublicUserResponse response = userService.getPublicProfile(1L);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.username()).isEqualTo("testuser");
            assertThat(response.avatarUrl()).isEqualTo("https://example.com/avatar.jpg");
            assertThat(response.bio()).isEqualTo("Hello world");
            assertThat(response.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getPublicProfile(999L))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("should update bio only")
        void shouldUpdateBioOnly() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            UpdateProfileRequest request = new UpdateProfileRequest("New bio", null, null);
            UserResponse response = userService.updateProfile("testuser", request);

            assertThat(testUser.getBio()).isEqualTo("New bio");
            assertThat(testUser.getPhone()).isEqualTo("+71234567890");
            assertThat(testUser.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("should update phone only")
        void shouldUpdatePhoneOnly() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            UpdateProfileRequest request = new UpdateProfileRequest(null, "+79999999999", null);
            userService.updateProfile("testuser", request);

            assertThat(testUser.getPhone()).isEqualTo("+79999999999");
            assertThat(testUser.getBio()).isEqualTo("Hello world");
        }

        @Test
        @DisplayName("should update avatar only")
        void shouldUpdateAvatarOnly() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            UpdateProfileRequest request = new UpdateProfileRequest(null, null, "https://new-avatar.jpg");
            userService.updateProfile("testuser", request);

            assertThat(testUser.getAvatarUrl()).isEqualTo("https://new-avatar.jpg");
        }

        @Test
        @DisplayName("should update all fields")
        void shouldUpdateAllFields() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            UpdateProfileRequest request = new UpdateProfileRequest(
                    "Updated bio", "+79999999999", "https://new-avatar.jpg"
            );
            userService.updateProfile("testuser", request);

            assertThat(testUser.getBio()).isEqualTo("Updated bio");
            assertThat(testUser.getPhone()).isEqualTo("+79999999999");
            assertThat(testUser.getAvatarUrl()).isEqualTo("https://new-avatar.jpg");
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenNotFound() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            UpdateProfileRequest request = new UpdateProfileRequest("bio", null, null);

            assertThatThrownBy(() -> userService.updateProfile("unknown", request))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteAccount")
    class DeleteAccount {

        @Test
        @DisplayName("should delete account and revoke tokens")
        void shouldDeleteAccount() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            userService.deleteAccount("testuser");

            verify(tokenRepository).revokeAllByUserId(1L);
            verify(userRepository).delete(testUser);
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenNotFound() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteAccount("unknown"))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository, never()).delete(any());
        }
    }
}