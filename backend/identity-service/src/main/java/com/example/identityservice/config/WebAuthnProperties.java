package com.example.identityservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.webauthn")
public record WebAuthnProperties(
        String rpId,
        String rpName,
        String origin,
        long timeout
) {
    public WebAuthnProperties {
        if (rpId == null || rpId.isBlank()) rpId = "localhost";
        if (rpName == null || rpName.isBlank()) rpName = "Pulse";
        if (origin == null || origin.isBlank()) origin = "http://localhost:3000";
        if (timeout <= 0) timeout = 60000;
    }
}
