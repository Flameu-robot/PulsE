package com.example.identityservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.oauth2")
public record OAuth2Properties(
        List<String> authorizedRedirectUris,
        String defaultRedirectUri
) {
    public OAuth2Properties {
        if (authorizedRedirectUris == null) {
            authorizedRedirectUris = List.of();
        }
        if (defaultRedirectUri == null || defaultRedirectUri.isBlank()) {
            defaultRedirectUri = "/";
        }
    }
}
