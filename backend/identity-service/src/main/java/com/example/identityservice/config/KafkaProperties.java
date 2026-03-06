package com.example.identityservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProperties(
        Topics topics
) {
    public record Topics(
            String userCreated,
            String userUpdated,
            String userBanned
    ) {}
}
