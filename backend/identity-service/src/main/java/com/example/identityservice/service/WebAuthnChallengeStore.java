package com.example.identityservice.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class WebAuthnChallengeStore {

    private static final String KEY_PREFIX = "webauthn:flow:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;

    public WebAuthnChallengeStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String save(String json) {
        String flowId = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(KEY_PREFIX + flowId, json, TTL);
        return flowId;
    }

    public String getAndRemove(String flowId) {
        String key = KEY_PREFIX + flowId;
        String json = redisTemplate.opsForValue().getAndDelete(key);
        if (json == null) {
            throw new RuntimeException("WebAuthn flow expired or not found: " + flowId);
        }
        return json;
    }
}
