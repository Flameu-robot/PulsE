package com.example.feedservice.service;

import com.example.feedservice.entity.Post;
import com.example.feedservice.entity.PostLike;
import com.example.feedservice.entity.enums.PostVisibility;
import com.example.feedservice.repository.PostLikeRepository;
import com.example.feedservice.repository.PostStatsRepository;
import exception.feed.DuplicateLikeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock PostLikeRepository likeRepository;
    @Mock PostStatsRepository statsRepository;
    @Mock PostService postService;
    @Mock InteractionService interactionService;
    @InjectMocks LikeService likeService;

    @Nested
    @DisplayName("likePost")
    class LikePost {

        @Test
        @DisplayName("should like post and increment stats")
        void shouldLikePost() {
            var post = buildPost(1L, 10L);
            when(likeRepository.existsByPostIdAndUserId(1L, 5L)).thenReturn(false);
            when(postService.getPostOrThrow(1L)).thenReturn(post);
            when(likeRepository.save(any(PostLike.class))).thenAnswer(inv -> inv.getArgument(0));

            likeService.likePost(1L, 5L);

            verify(likeRepository).save(any(PostLike.class));
            verify(statsRepository).incrementLikes(1L);
            verify(interactionService).onLike(5L, 10L);
        }

        @Test
        @DisplayName("should throw on duplicate like")
        void shouldThrowOnDuplicate() {
            when(likeRepository.existsByPostIdAndUserId(1L, 5L)).thenReturn(true);

            assertThatThrownBy(() -> likeService.likePost(1L, 5L))
                    .isInstanceOf(DuplicateLikeException.class);

            verify(likeRepository, never()).save(any());
            verify(statsRepository, never()).incrementLikes(any());
            verify(interactionService, never()).onLike(any(), any());
        }
    }

    private Post buildPost(Long id, Long authorId) {
        return Post.builder()
                .id(id).authorId(authorId).content("Test")
                .visibility(PostVisibility.PUBLIC)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
    }
}