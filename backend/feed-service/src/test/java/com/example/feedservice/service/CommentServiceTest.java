package com.example.feedservice.service;

import com.example.feedservice.dto.request.CreateCommentRequest;
import com.example.feedservice.dto.response.CommentResponse;
import com.example.feedservice.entity.Comment;
import com.example.feedservice.entity.Post;
import com.example.feedservice.entity.enums.PostVisibility;
import com.example.feedservice.repository.CommentRepository;
import com.example.feedservice.repository.PostStatsRepository;
import exception.feed.PostAccessDeniedException;
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
class CommentServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock PostStatsRepository statsRepository;
    @Mock PostService postService;
    @InjectMocks CommentService commentService;

    @Nested
    @DisplayName("addComment")
    class AddComment {

        @Test
        @DisplayName("should create comment and increment stats")
        void shouldCreateComment() {
            var post = buildPost(1L, 10L);
            when(postService.getPostOrThrow(1L)).thenReturn(post);
            when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
                Comment c = inv.getArgument(0);
                c.setId(1L);
                c.setCreatedAt(Instant.now());
                return c;
            });

            var request = new CreateCommentRequest("Nice post!", null);
            CommentResponse response = commentService.addComment(1L, 5L, request);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.content()).isEqualTo("Nice post!");
            assertThat(response.postId()).isEqualTo(1L);
            verify(statsRepository).incrementComments(1L);
        }
    }

    @Nested
    @DisplayName("deleteComment")
    class DeleteComment {

        @Test
        @DisplayName("should throw when user is neither comment nor post author")
        void shouldThrowWhenNotAuthor() {
            var post = buildPost(1L, 10L);
            var comment = Comment.builder()
                    .id(1L).post(post).authorId(20L)
                    .content("text").createdAt(Instant.now())
                    .build();

            when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentService.deleteComment(1L, 999L))
                    .isInstanceOf(PostAccessDeniedException.class);

            verify(commentRepository, never()).delete(any());
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
