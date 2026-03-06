package com.example.identityservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebAuthnChallengeStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    private WebAuthnChallengeStore challengeStore;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        challengeStore = new WebAuthnChallengeStore(redisTemplate);
    }

    @Test
    @DisplayName("should save json and return flowId")
    void shouldSaveAndReturnFlowId() {
        String json = "{\"challenge\":\"abc\"}";

        String flowId = challengeStore.save(json);

        assertThat(flowId).isNotBlank();
        assertThat(flowId).hasSize(36);

        verify(valueOps).set(
                eq("webauthn:flow:" + flowId),
                eq(json),
                eq(Duration.ofMinutes(5))
        );
    }

    @Test
    @DisplayName("should get and remove json by flowId")
    void shouldGetAndRemove() {
        String json = "{\"challenge\":\"abc\"}";

        when(valueOps.getAndDelete("webauthn:flow:test-id"))
                .thenReturn(json);

        String result = challengeStore.getAndRemove("test-id");

        assertThat(result).isEqualTo(json);
        verify(valueOps).getAndDelete("webauthn:flow:test-id");
    }

    @Test
    @DisplayName("should throw when flow not found")
    void shouldThrowWhenNotFound() {
        when(valueOps.getAndDelete("webauthn:flow:expired-id"))
                .thenReturn(null);

        assertThatThrownBy(() -> challengeStore.getAndRemove("expired-id"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("expired or not found");
    }

    @Test
    @DisplayName("should generate unique flowIds")
    void shouldGenerateUniqueIds() {
        String id1 = challengeStore.save("json1");
        String id2 = challengeStore.save("json2");

        assertThat(id1).isNotEqualTo(id2);
    }
}
