package com.example.feedservice.service;

import com.example.feedservice.dto.request.CreatePostRequest;
import com.example.feedservice.dto.request.UpdatePostRequest;
import com.example.feedservice.dto.response.PostDetailResponse;
import com.example.feedservice.dto.response.PostResponse;
import com.example.feedservice.entity.Post;
import com.example.feedservice.entity.PostStats;
import com.example.feedservice.entity.enums.PostVisibility;
import com.example.feedservice.repository.*;
import exception.feed.PostAccessDeniedException;
import exception.feed.PostNotFoundException;
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
class PostServiceTest {

    @Mock PostRepository postRepository;
    @Mock PostStatsRepository statsRepository;
    @Mock PostLikeRepository likeRepository;
    @Mock BookmarkRepository bookmarkRepository;
    @Mock FollowRepository followRepository;
    @InjectMocks PostService postService;

    @Nested
    @DisplayName("createPost")
    class CreatePost {

        @Test
        @DisplayName("should create post and return response")
        void shouldCreatePost() {
            var request = new CreatePostRequest("Hello world", PostVisibility.PUBLIC, null, null);

            when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
                Post p = inv.getArgument(0);
                p.setId(1L);
                p.setCreatedAt(Instant.now());
                p.setUpdatedAt(Instant.now());
                return p;
            });
            when(statsRepository.save(any(PostStats.class))).thenAnswer(inv -> inv.getArgument(0));

            PostResponse response = postService.createPost(10L, request);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.authorId()).isEqualTo(10L);
            assertThat(response.content()).isEqualTo("Hello world");
            assertThat(response.visibility()).isEqualTo(PostVisibility.PUBLIC);
            verify(postRepository).save(any(Post.class));
            verify(statsRepository).save(any(PostStats.class));
        }
    }

    @Nested
    @DisplayName("getPostDetail")
    class GetPostDetail {

        @Test
        @DisplayName("should return post detail with user context")
        void shouldReturnPostDetail() {
            Post post = buildPost(1L, 10L);
            when(postRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(post));
            when(likeRepository.existsByPostIdAndUserId(1L, 5L)).thenReturn(true);
            when(bookmarkRepository.existsByUserIdAndPostId(5L, 1L)).thenReturn(false);

            PostDetailResponse response = postService.getPostDetail(1L, 5L);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.likedByMe()).isTrue();
            assertThat(response.bookmarkedByMe()).isFalse();
        }

        @Test
        @DisplayName("should throw PostNotFoundException when post not found")
        void shouldThrowWhenNotFound() {
            when(postRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> postService.getPostDetail(99L, 1L))
                    .isInstanceOf(PostNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updatePost")
    class UpdatePost {

        @Test
        @DisplayName("should update post by owner")
        void shouldUpdateByOwner() {
            Post post = buildPost(1L, 10L);
            when(postRepository.findById(1L)).thenReturn(Optional.of(post));
            when(postRepository.save(any())).thenReturn(post);
            when(likeRepository.existsByPostIdAndUserId(1L, 10L)).thenReturn(false);
            when(bookmarkRepository.existsByUserIdAndPostId(10L, 1L)).thenReturn(false);

            var request = new UpdatePostRequest("Updated content", null, null);
            PostResponse response = postService.updatePost(1L, 10L, request);

            assertThat(response.content()).isEqualTo("Updated content");
        }

        @Test
        @DisplayName("should throw PostAccessDeniedException when not owner")
        void shouldThrowWhenNotOwner() {
            Post post = buildPost(1L, 10L);
            when(postRepository.findById(1L)).thenReturn(Optional.of(post));

            var request = new UpdatePostRequest("Hacked", null, null);

            assertThatThrownBy(() -> postService.updatePost(1L, 999L, request))
                    .isInstanceOf(PostAccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("deletePost")
    class DeletePost {

        @Test
        @DisplayName("should delete post by owner")
        void shouldDeleteByOwner() {
            Post post = buildPost(1L, 10L);
            when(postRepository.findById(1L)).thenReturn(Optional.of(post));

            postService.deletePost(1L, 10L);

            verify(postRepository).delete(post);
        }

        @Test
        @DisplayName("should throw PostAccessDeniedException when not owner")
        void shouldThrowWhenNotOwner() {
            Post post = buildPost(1L, 10L);
            when(postRepository.findById(1L)).thenReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.deletePost(1L, 999L))
                    .isInstanceOf(PostAccessDeniedException.class);

            verify(postRepository, never()).delete(any());
        }
    }

    private Post buildPost(Long id, Long authorId) {
        var post = Post.builder()
                .id(id)
                .authorId(authorId)
                .content("Test content")
                .visibility(PostVisibility.PUBLIC)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        post.setStats(PostStats.builder().post(post).build());
        return post;
    }
}