package com.example.feedservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Gateway gateway,
        Kafka kafka
) {
    public record Gateway(String url) {}

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
