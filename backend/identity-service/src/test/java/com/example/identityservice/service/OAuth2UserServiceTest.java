package com.example.identityservice.service;

import com.example.identityservice.dto.OAuth2UserInfo;
import com.example.identityservice.entity.OAuth2Account;
import com.example.identityservice.entity.User;
import com.example.identityservice.entity.enums.OAuthProvider;
import com.example.identityservice.entity.enums.UserRole;
import com.example.identityservice.entity.enums.UserStatus;
import com.example.identityservice.repository.OAuth2Repository;
import com.example.identityservice.repository.UserRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2Repository oAuth2Repository;

    @InjectMocks
    private OAuth2UserService oAuth2UserService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<OAuth2Account> accountCaptor;

    private OAuth2UserInfo googleUserInfo;
    private OAuth2UserInfo githubUserInfo;
    private User existingUser;
    private OAuth2Account existingAccount;

    @BeforeEach
    void setUp() {
        googleUserInfo = new OAuth2UserInfo(
                "google-123",
                "john@gmail.com",
                "John Doe",
                "https://google.com/avatar.jpg",
                OAuthProvider.GOOGLE
        );

        githubUserInfo = new OAuth2UserInfo(
                "12345",
                "john@github.com",
                "johndoe",
                "https://github.com/avatar.jpg",
                OAuthProvider.GITHUB
        );

        existingUser = User.builder()
                .id(1L)
                .username("existinguser")
                .email("john@gmail.com")
                .passwordHash("$2a$10$hash")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        existingAccount = OAuth2Account.builder()
                .id(1L)
                .user(existingUser)
                .provider(OAuthProvider.GOOGLE)
                .providerId("google-123")
                .build();
    }

    @Nested
    @DisplayName("processOAuth2User - existing OAuth link")
    class ExistingOAuthLink {

        @Test
        @DisplayName("should return user when OAuth link already exists")
        void shouldReturnUserWhenLinkExists() {
            when(oAuth2Repository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "google-123"))
                    .thenReturn(Optional.of(existingAccount));
            when(userRepository.save(any(User.class))).thenReturn(existingUser);

            User result = oAuth2UserService.processOAuth2User(googleUserInfo);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("existinguser");

            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getLastLoginAt()).isNotNull();

            verify(oAuth2Repository, never()).save(any());
            verify(userRepository, never()).findByEmail(any());
        }

        @Test
        @DisplayName("should update avatar if user has none")
        void shouldUpdateAvatarIfEmpty() {
            existingUser.setAvatarUrl(null);
            when(oAuth2Repository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "google-123"))
                    .thenReturn(Optional.of(existingAccount));
            when(userRepository.save(any(User.class))).thenReturn(existingUser);

            oAuth2UserService.processOAuth2User(googleUserInfo);

            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getAvatarUrl())
                    .isEqualTo("https://google.com/avatar.jpg");
        }

        @Test
        @DisplayName("should not overwrite existing avatar")
        void shouldNotOverwriteExistingAvatar() {
            existingUser.setAvatarUrl("https://my-custom-avatar.jpg");
            when(oAuth2Repository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "google-123"))
                    .thenReturn(Optional.of(existingAccount));
            when(userRepository.save(any(User.class))).thenReturn(existingUser);

            oAuth2UserService.processOAuth2User(googleUserInfo);

            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getAvatarUrl())
                    .isEqualTo("https://my-custom-avatar.jpg");
        }
    }

    @Nested
    @DisplayName("processOAuth2User - link to existing user by email")
    class LinkToExistingUser {

        @Test
        @DisplayName("should link OAuth account to existing user with same email")
        void shouldLinkToExistingUser() {
            when(oAuth2Repository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "google-123"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmail("john@gmail.com"))
                    .thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(existingUser);
            when(oAuth2Repository.save(any(OAuth2Account.class))).thenReturn(existingAccount);

            User result = oAuth2UserService.processOAuth2User(googleUserInfo);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("existinguser");

            verify(oAuth2Repository).save(accountCaptor.capture());
            OAuth2Account savedAccount = accountCaptor.getValue();
            assertThat(savedAccount.getProvider()).isEqualTo(OAuthProvider.GOOGLE);
            assertThat(savedAccount.getProviderId()).isEqualTo("google-123");
            assertThat(savedAccount.getUser().getId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("processOAuth2User - create new user")
    class CreateNewUser {

        @Test
        @DisplayName("should create new user when no existing link or email match")
        void shouldCreateNewUser() {
            when(oAuth2Repository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "google-123"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmail("john@gmail.com"))
                    .thenReturn(Optional.empty());
            when(userRepository.existsByUsername("john_doe")).thenReturn(false);

            User newUser = User.builder()
                    .id(2L)
                    .username("john_doe")
                    .email("john@gmail.com")
                    .status(UserStatus.ACTIVE)
                    .role(UserRole.USER)
                    .avatarUrl("https://google.com/avatar.jpg")
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(newUser);
            when(oAuth2Repository.save(any(OAuth2Account.class))).thenReturn(new OAuth2Account());

            User result = oAuth2UserService.processOAuth2User(googleUserInfo);

            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getUsername()).isEqualTo("john_doe");

            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getPasswordHash()).isNull();
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
            assertThat(savedUser.getAvatarUrl()).isEqualTo("https://google.com/avatar.jpg");

            verify(oAuth2Repository).save(accountCaptor.capture());
            OAuth2Account savedAccount = accountCaptor.getValue();
            assertThat(savedAccount.getProvider()).isEqualTo(OAuthProvider.GOOGLE);
            assertThat(savedAccount.getProviderId()).isEqualTo("google-123");
        }

        @Test
        @DisplayName("should generate unique username when base is taken")
        void shouldGenerateUniqueUsername() {
            when(oAuth2Repository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "google-123"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmail("john@gmail.com"))
                    .thenReturn(Optional.empty());
            when(userRepository.existsByUsername("john_doe")).thenReturn(true);
            when(userRepository.existsByUsername(argThat(name ->
                    name.startsWith("john_doe_") && name.length() > 9
            ))).thenReturn(false);

            User newUser = User.builder().id(2L).username("john_doe_a1b2").build();
            when(userRepository.save(any(User.class))).thenReturn(newUser);
            when(oAuth2Repository.save(any(OAuth2Account.class))).thenReturn(new OAuth2Account());

            oAuth2UserService.processOAuth2User(googleUserInfo);

            verify(userRepository).save(userCaptor.capture());
            String generatedUsername = userCaptor.getValue().getUsername();
            assertThat(generatedUsername).startsWith("john_doe_");
            assertThat(generatedUsername).hasSize(13);
        }

        @Test
        @DisplayName("should handle null name from provider")
        void shouldHandleNullName() {
            OAuth2UserInfo infoWithoutName = new OAuth2UserInfo(
                    "google-456",
                    "anon@gmail.com",
                    null,
                    null,
                    OAuthProvider.GOOGLE
            );

            when(oAuth2Repository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "google-456"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmail("anon@gmail.com"))
                    .thenReturn(Optional.empty());
            when(userRepository.existsByUsername("user")).thenReturn(false);

            User newUser = User.builder().id(3L).username("user").build();
            when(userRepository.save(any(User.class))).thenReturn(newUser);
            when(oAuth2Repository.save(any(OAuth2Account.class))).thenReturn(new OAuth2Account());

            oAuth2UserService.processOAuth2User(infoWithoutName);

            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getUsername()).isEqualTo("user");
        }

        @Test
        @DisplayName("should handle null email from provider")
        void shouldHandleNullEmail() {
            OAuth2UserInfo infoWithoutEmail = new OAuth2UserInfo(
                    "github-789",
                    null,
                    "privateuser",
                    "https://github.com/avatar.jpg",
                    OAuthProvider.GITHUB
            );

            when(oAuth2Repository.findByProviderAndProviderId(OAuthProvider.GITHUB, "github-789"))
                    .thenReturn(Optional.empty());
            when(userRepository.existsByUsername("privateuser")).thenReturn(false);

            User newUser = User.builder().id(4L).username("privateuser").build();
            when(userRepository.save(any(User.class))).thenReturn(newUser);
            when(oAuth2Repository.save(any(OAuth2Account.class))).thenReturn(new OAuth2Account());

            oAuth2UserService.processOAuth2User(infoWithoutEmail);

            verify(userRepository, never()).findByEmail(any());
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("processOAuth2User - GitHub provider")
    class GitHubProvider {

        @Test
        @DisplayName("should process GitHub user correctly")
        void shouldProcessGitHubUser() {
            when(oAuth2Repository.findByProviderAndProviderId(OAuthProvider.GITHUB, "12345"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmail("john@github.com"))
                    .thenReturn(Optional.empty());
            when(userRepository.existsByUsername("johndoe")).thenReturn(false);

            User newUser = User.builder()
                    .id(5L)
                    .username("johndoe")
                    .email("john@github.com")
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(newUser);
            when(oAuth2Repository.save(any(OAuth2Account.class))).thenReturn(new OAuth2Account());

            User result = oAuth2UserService.processOAuth2User(githubUserInfo);

            assertThat(result.getUsername()).isEqualTo("johndoe");

            verify(oAuth2Repository).save(accountCaptor.capture());
            assertThat(accountCaptor.getValue().getProvider()).isEqualTo(OAuthProvider.GITHUB);
            assertThat(accountCaptor.getValue().getProviderId()).isEqualTo("12345");
        }
    }
}
