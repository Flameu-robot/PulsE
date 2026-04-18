package com.example.feedservice.service;

import com.example.feedservice.dto.response.FollowResponse;
import com.example.feedservice.entity.Follow;
import com.example.feedservice.entity.enums.FollowStatus;
import com.example.feedservice.repository.FollowRepository;
import exception.feed.DuplicateFollowException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock FollowRepository followRepository;
    @InjectMocks FollowService followService;

    @Nested
    @DisplayName("follow")
    class FollowAction {

        @Test
        @DisplayName("should create follow successfully")
        void shouldFollow() {
            when(followRepository.findByFollowerIdAndFolloweeId(1L, 2L)).thenReturn(Optional.empty());
            when(followRepository.save(any(Follow.class))).thenAnswer(inv -> {
                Follow f = inv.getArgument(0);
                f.setId(1L);
                f.setCreatedAt(Instant.now());
                return f;
            });

            FollowResponse response = followService.follow(1L, 2L);

            assertThat(response.followerId()).isEqualTo(1L);
            assertThat(response.followeeId()).isEqualTo(2L);
            assertThat(response.status()).isEqualTo(FollowStatus.ACTIVE);
            verify(followRepository).save(any(Follow.class));
        }

        @Test
        @DisplayName("should throw when following self")
        void shouldThrowOnSelfFollow() {
            assertThatThrownBy(() -> followService.follow(1L, 1L))
                    .isInstanceOf(DuplicateFollowException.class);

            verify(followRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when already following")
        void shouldThrowOnDuplicate() {
            var existing = Follow.builder().id(1L).followerId(1L).followeeId(2L).build();
            when(followRepository.findByFollowerIdAndFolloweeId(1L, 2L)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> followService.follow(1L, 2L))
                    .isInstanceOf(DuplicateFollowException.class);
        }
    }
}
