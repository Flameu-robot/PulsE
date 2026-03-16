package com.example.feedservice.service;

import com.example.feedservice.dto.response.BookmarkResponse;
import com.example.feedservice.entity.Bookmark;
import com.example.feedservice.entity.Post;
import com.example.feedservice.entity.enums.PostVisibility;
import com.example.feedservice.repository.BookmarkRepository;
import com.example.feedservice.repository.PostLikeRepository;
import exception.feed.DuplicateBookmarkException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock BookmarkRepository bookmarkRepository;
    @Mock PostLikeRepository likeRepository;
    @Mock PostService postService;
    @InjectMocks BookmarkService bookmarkService;

    @Nested
    @DisplayName("addBookmark")
    class AddBookmark {

        @Test
        @DisplayName("should add bookmark successfully")
        void shouldAddBookmark() {
            var post = buildPost(1L, 10L);
            when(bookmarkRepository.existsByUserIdAndPostId(5L, 1L)).thenReturn(false);
            when(postService.getPostOrThrow(1L)).thenReturn(post);
            when(bookmarkRepository.save(any(Bookmark.class))).thenAnswer(inv -> {
                Bookmark b = inv.getArgument(0);
                b.setId(1L);
                b.setCreatedAt(Instant.now());
                return b;
            });

            BookmarkResponse response = bookmarkService.addBookmark(5L, 1L);

            assertThat(response.postId()).isEqualTo(1L);
            verify(bookmarkRepository).save(any(Bookmark.class));
        }

        @Test
        @DisplayName("should throw on duplicate bookmark")
        void shouldThrowOnDuplicate() {
            when(bookmarkRepository.existsByUserIdAndPostId(5L, 1L)).thenReturn(true);

            assertThatThrownBy(() -> bookmarkService.addBookmark(5L, 1L))
                    .isInstanceOf(DuplicateBookmarkException.class);

            verify(bookmarkRepository, never()).save(any());
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
