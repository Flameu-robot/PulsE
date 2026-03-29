package com.example.feedservice.controller;

import com.example.feedservice.dto.response.BookmarkResponse;
import com.example.feedservice.dto.response.PagedResponse;
import com.example.feedservice.dto.response.PostResponse;
import com.example.feedservice.dto.response.PostStatsResponse;
import com.example.feedservice.entity.enums.PostVisibility;
import com.example.feedservice.service.BookmarkService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookmarkService bookmarkService;

    private PostResponse createSamplePostResponse(Long id, Long authorId) {
        return new PostResponse(
                id,
                authorId,
                "Test content",
                PostVisibility.PUBLIC,
                false,
                List.of(),
                List.of(),
                List.of(),  // groupIds
                new PostStatsResponse(0, 0, 0, 0),
                Instant.now(),
                Instant.now(),
                true,   // likedByMe
                true    // bookmarkedByMe
        );
    }

    @Nested
    @DisplayName("POST /api/posts/{postId}/bookmarks")
    class AddBookmark {

        @Test
        @DisplayName("should return 201 on successful bookmark")
        void shouldReturn201() throws Exception {
            var response = new BookmarkResponse(1L, 10L, Instant.now());
            when(bookmarkService.addBookmark(1L, 10L)).thenReturn(response);

            mockMvc.perform(post("/api/posts/10/bookmarks")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.postId").value(10));
        }
    }

    @Nested
    @DisplayName("DELETE /api/posts/{postId}/bookmarks")
    class RemoveBookmark {

        @Test
        @DisplayName("should return 204 on successful removal")
        void shouldReturn204() throws Exception {
            mockMvc.perform(delete("/api/posts/10/bookmarks")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isNoContent());

            verify(bookmarkService).removeBookmark(1L, 10L);
        }
    }

    @Nested
    @DisplayName("GET /api/posts/{postId}/bookmarks/status")
    class CheckStatus {

        @Test
        @DisplayName("should return 200 with bookmark status true")
        void shouldReturn200WithTrue() throws Exception {
            when(bookmarkService.isBookmarked(1L, 10L)).thenReturn(true);

            mockMvc.perform(get("/api/posts/10/bookmarks/status")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bookmarked").value(true));
        }

        @Test
        @DisplayName("should return 200 with bookmark status false")
        void shouldReturn200WithFalse() throws Exception {
            when(bookmarkService.isBookmarked(1L, 10L)).thenReturn(false);

            mockMvc.perform(get("/api/posts/10/bookmarks/status")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bookmarked").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/bookmarks")
    class MyBookmarks {

        @Test
        @DisplayName("should return 200 with bookmarked posts")
        void shouldReturn200() throws Exception {
            var post = createSamplePostResponse(10L, 2L);
            var page = new PagedResponse<>(List.of(post), 0, 20, 1, 1, true);
            when(bookmarkService.getBookmarkedPosts(eq(1L), any())).thenReturn(page);

            mockMvc.perform(get("/api/bookmarks")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(10))
                    .andExpect(jsonPath("$.content[0].bookmarkedByMe").value(true));
        }

        @Test
        @DisplayName("should return empty when no bookmarks")
        void shouldReturnEmptyWhenNoBookmarks() throws Exception {
            var emptyPage = new PagedResponse<PostResponse>(List.of(), 0, 20, 0, 0, true);
            when(bookmarkService.getBookmarkedPosts(eq(1L), any())).thenReturn(emptyPage);

            mockMvc.perform(get("/api/bookmarks")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }
}