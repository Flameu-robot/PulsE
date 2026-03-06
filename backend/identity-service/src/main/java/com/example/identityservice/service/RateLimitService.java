package com.example.identityservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);
    private static final String KEY_PREFIX = "rate:";

    private final StringRedisTemplate redisTemplate;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isRateLimited(String ip, String endpoint, int maxAttempts, Duration window) {
        String key = KEY_PREFIX + ip + ":" + endpoint;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, window);
        }

        if (count != null && count > maxAttempts) {
            log.warn("Rate limit exceeded: ip={}, endpoint={}, count={}", ip, endpoint, count);
            return true;
        }

        return false;
    }

    public long getRetryAfterSeconds(String ip, String endpoint) {
        String key = KEY_PREFIX + ip + ":" + endpoint;
        Long ttl = redisTemplate.getExpire(key);
        return ttl != null && ttl > 0 ? ttl : 60;
    }
}
