package com.example.identityservice.dto;

import com.example.identityservice.entity.enums.OAuthProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2UserInfoTest {

    @Nested
    @DisplayName("Google provider")
    class GoogleProvider {

        @Test
        @DisplayName("should parse Google attributes correctly")
        void shouldParseGoogleAttributes() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", "google-user-id-123");
            attributes.put("email", "user@gmail.com");
            attributes.put("name", "John Doe");
            attributes.put("picture", "https://lh3.googleusercontent.com/photo.jpg");

            OAuth2UserInfo userInfo = OAuth2UserInfo.of(OAuthProvider.GOOGLE, attributes);

            assertThat(userInfo.providerId()).isEqualTo("google-user-id-123");
            assertThat(userInfo.email()).isEqualTo("user@gmail.com");
            assertThat(userInfo.name()).isEqualTo("John Doe");
            assertThat(userInfo.avatarUrl()).isEqualTo("https://lh3.googleusercontent.com/photo.jpg");
            assertThat(userInfo.provider()).isEqualTo(OAuthProvider.GOOGLE);
        }

        @Test
        @DisplayName("should handle null values from Google")
        void shouldHandleNullValues() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", "google-123");

            OAuth2UserInfo userInfo = OAuth2UserInfo.of(OAuthProvider.GOOGLE, attributes);

            assertThat(userInfo.providerId()).isEqualTo("google-123");
            assertThat(userInfo.email()).isNull();
            assertThat(userInfo.name()).isNull();
            assertThat(userInfo.avatarUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("GitHub provider")
    class GitHubProvider {

        @Test
        @DisplayName("should parse GitHub attributes correctly")
        void shouldParseGitHubAttributes() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("id", 12345678);
            attributes.put("email", "user@github.com");
            attributes.put("login", "johndoe");
            attributes.put("avatar_url", "https://avatars.githubusercontent.com/u/12345678");

            OAuth2UserInfo userInfo = OAuth2UserInfo.of(OAuthProvider.GITHUB, attributes);

            assertThat(userInfo.providerId()).isEqualTo("12345678");
            assertThat(userInfo.email()).isEqualTo("user@github.com");
            assertThat(userInfo.name()).isEqualTo("johndoe");
            assertThat(userInfo.avatarUrl()).isEqualTo("https://avatars.githubusercontent.com/u/12345678");
            assertThat(userInfo.provider()).isEqualTo(OAuthProvider.GITHUB);
        }

        @Test
        @DisplayName("should handle GitHub user with private email")
        void shouldHandlePrivateEmail() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("id", 87654321);
            attributes.put("email", null);
            attributes.put("login", "privateuser");
            attributes.put("avatar_url", "https://avatars.githubusercontent.com/u/87654321");

            OAuth2UserInfo userInfo = OAuth2UserInfo.of(OAuthProvider.GITHUB, attributes);

            assertThat(userInfo.providerId()).isEqualTo("87654321");
            assertThat(userInfo.email()).isNull();
            assertThat(userInfo.name()).isEqualTo("privateuser");
        }

        @Test
        @DisplayName("should convert numeric id to string")
        void shouldConvertNumericIdToString() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("id", 999);
            attributes.put("login", "testuser");

            OAuth2UserInfo userInfo = OAuth2UserInfo.of(OAuthProvider.GITHUB, attributes);

            assertThat(userInfo.providerId()).isEqualTo("999");
            assertThat(userInfo.providerId()).isInstanceOf(String.class);
        }
    }
}
