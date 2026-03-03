package com.example.identityservice.dto;

import com.example.identityservice.entity.enums.OAuthProvider;

import java.util.Map;

public record OAuth2UserInfo(
        String providerId,
        String email,
        String name,
        String avatarUrl,
        OAuthProvider provider
) {

    public static OAuth2UserInfo of(OAuthProvider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case GOOGLE -> new OAuth2UserInfo(
                    (String) attributes.get("sub"),
                    (String) attributes.get("email"),
                    (String) attributes.get("name"),
                    (String) attributes.get("picture"),
                    provider
            );
            case GITHUB -> new OAuth2UserInfo(
                    String.valueOf(attributes.get("id")),
                    (String) attributes.get("email"),
                    (String) attributes.get("login"),
                    (String) attributes.get("avatar_url"),
                    provider
            );
        };
    }
}
