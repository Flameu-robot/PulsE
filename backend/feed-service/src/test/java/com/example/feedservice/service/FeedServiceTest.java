package com.example.feedservice.service;

import com.example.feedservice.dto.response.PagedResponse;
import com.example.feedservice.dto.response.PostResponse;
import com.example.feedservice.dto.response.PostStatsResponse;
import com.example.feedservice.entity.Post;
import com.example.feedservice.entity.PostStats;
import com.example.feedservice.entity.enums.PostVisibility;
import com.example.feedservice.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock PostRepository postRepository;
    @Mock FollowRepository followRepository;
    @Mock PostLikeRepository likeRepository;
    @Mock BookmarkRepository bookmarkRepository;
    @Mock PostService postService;
    @InjectMocks FeedService feedService;

    @Nested
    @DisplayName("getFollowingFeed")
    class GetFollowingFeed {

        @Test
        @DisplayName("should return posts from followed users")
        void shouldReturnPostsFromFollows() {
            Long userId = 1L;
            when(followRepository.findActiveFolloweeIds(userId)).thenReturn(List.of(2L, 3L));

            Post post = buildPost(10L, 2L);
            when(postRepository.findFeedByAuthors(anyList(), anySet(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(post)));
            when(likeRepository.findByPostIdInAndUserId(any(), eq(userId))).thenReturn(List.of());
            when(bookmarkRepository.findBookmarkedPostIds(eq(userId), any())).thenReturn(List.of());

            var postResponse = new PostResponse(10L, 2L, "Test", PostVisibility.PUBLIC,
                    false, List.of(), List.of(), new PostStatsResponse(0, 0, 0, 0),
                    Instant.now(), Instant.now(), false, false);
            when(postService.enrichWithUserContext(any(Post.class), eq(userId))).thenReturn(postResponse);

            PagedResponse<PostResponse> result = feedService.getFollowingFeed(userId, Pageable.ofSize(20));

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().getFirst().id()).isEqualTo(10L);
        }

        @Test
        @DisplayName("should return empty when no follows")
        void shouldReturnEmptyWhenNoFollows() {
            when(followRepository.findActiveFolloweeIds(1L)).thenReturn(List.of());

            PagedResponse<PostResponse> result = feedService.getFollowingFeed(1L, Pageable.ofSize(20));

            assertThat(result.content()).isEmpty();
            verify(postRepository, never()).findFeedByAuthors(any(), any(), any());
        }
    }

    private Post buildPost(Long id, Long authorId) {
        var post = Post.builder()
                .id(id).authorId(authorId).content("Test")
                .visibility(PostVisibility.PUBLIC)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
        post.setStats(PostStats.builder().post(post).build());
        return post;
    }
}
