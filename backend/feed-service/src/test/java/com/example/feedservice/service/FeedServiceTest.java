package com.example.feedservice.service;

import com.example.feedservice.client.MessagingServiceClient;
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
import org.springframework.data.domain.Page;
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
    @Mock MessagingServiceClient messagingServiceClient;
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

            var postResponse = createPostResponse(10L, 2L);
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

    @Nested
    @DisplayName("getExploreFeed")
    class GetExploreFeed {

        @Test
        @DisplayName("should return public posts")
        void shouldReturnPublicPosts() {
            Post post = buildPost(1L, 10L);
            when(postRepository.findPublicPostsSince(any(), any()))
                    .thenReturn(new PageImpl<>(List.of(post)));

            var postResponse = createPostResponse(1L, 10L);
            when(postService.enrichWithUserContext(any(Post.class), any())).thenReturn(postResponse);

            PagedResponse<PostResponse> result = feedService.getExploreFeed(5L, Pageable.ofSize(20));

            assertThat(result.content()).hasSize(1);
        }

        @Test
        @DisplayName("should work for anonymous user")
        void shouldWorkForAnonymous() {
            Post post = buildPost(1L, 10L);
            when(postRepository.findPublicPostsSince(any(), any()))
                    .thenReturn(new PageImpl<>(List.of(post)));

            var postResponse = createPostResponse(1L, 10L);
            when(postService.enrichWithUserContext(any(Post.class), isNull())).thenReturn(postResponse);

            PagedResponse<PostResponse> result = feedService.getExploreFeed(null, Pageable.ofSize(20));

            assertThat(result.content()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getGroupsFeed")
    class GetGroupsFeed {

        @Test
        @DisplayName("should return posts from user groups")
        void shouldReturnPostsFromGroups() {
            Long userId = 1L;
            var groupIds = List.of(123L, 456L);

            when(messagingServiceClient.getUserGroupIds(userId)).thenReturn(groupIds);

            Post post = buildPostWithGroup(10L, 2L, 123L);
            when(postRepository.findByGroupIds(eq(groupIds), any()))
                    .thenReturn(new PageImpl<>(List.of(post)));
            when(likeRepository.findByPostIdInAndUserId(any(), eq(userId))).thenReturn(List.of());
            when(bookmarkRepository.findBookmarkedPostIds(eq(userId), any())).thenReturn(List.of());

            var postResponse = createPostResponseWithGroup(10L, 2L, 123L);
            when(postService.enrichWithUserContext(any(Post.class), eq(userId))).thenReturn(postResponse);

            PagedResponse<PostResponse> result = feedService.getGroupsFeed(userId, Pageable.ofSize(20));

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().getFirst().groupIds()).contains(123L);
            verify(messagingServiceClient).getUserGroupIds(userId);
        }

        @Test
        @DisplayName("should return empty when user has no groups")
        void shouldReturnEmptyWhenNoGroups() {
            when(messagingServiceClient.getUserGroupIds(1L)).thenReturn(List.of());

            PagedResponse<PostResponse> result = feedService.getGroupsFeed(1L, Pageable.ofSize(20));

            assertThat(result.content()).isEmpty();
            verify(postRepository, never()).findByGroupIds(any(), any());
        }
    }

    private Post buildPost(Long id, Long authorId) {
        var post = Post.builder()
                .id(id)
                .authorId(authorId)
                .content("Test")
                .visibility(PostVisibility.PUBLIC)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        post.setStats(PostStats.builder().post(post).build());
        return post;
    }

    private Post buildPostWithGroup(Long id, Long authorId, Long groupId) {
        var post = buildPost(id, authorId);
        post.addToGroup(groupId);
        return post;
    }

    private PostResponse createPostResponse(Long id, Long authorId) {
        return new PostResponse(
                id,
                authorId,
                "Test",
                PostVisibility.PUBLIC,
                false,
                List.of(),
                List.of(),
                List.of(),
                new PostStatsResponse(0, 0, 0, 0),
                Instant.now(),
                Instant.now(),
                false,
                false
        );
    }

    private PostResponse createPostResponseWithGroup(Long id, Long authorId, Long groupId) {
        return new PostResponse(
                id,
                authorId,
                "Test",
                PostVisibility.PUBLIC,
                false,
                List.of(),
                List.of(),
                List.of(groupId),
                new PostStatsResponse(0, 0, 0, 0),
                Instant.now(),
                Instant.now(),
                false,
                false
        );
    }
}