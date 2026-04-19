package com.example.feedservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Gateway gateway,
        Services services,
        Kafka kafka,
        InternalSecurity internalSecurity
) {
    public record Gateway(String url) {}

    public record Services(String messagingUrl) {}

    public record InternalSecurity(String headerName, String token) {}

    public record Kafka(Topics topics) {
        public record Topics(
                String userCreated,
                String userUpdated,
                String userBanned,
                String postCreated,
                String postUpdated,
                String postDeleted,
                String feedUpdated,
                String feedRebuilt
        ) {}
    }
}