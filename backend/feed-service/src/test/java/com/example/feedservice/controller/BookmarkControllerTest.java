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
        @DisplayName("should return 200 with bookmark status")
        void shouldReturn200() throws Exception {
            when(bookmarkService.isBookmarked(1L, 10L)).thenReturn(true);

            mockMvc.perform(get("/api/posts/10/bookmarks/status")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bookmarked").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/bookmarks")
    class MyBookmarks {

        @Test
        @DisplayName("should return 200 with bookmarked posts")
        void shouldReturn200() throws Exception {
            var post = new PostResponse(10L, 2L, "Test", PostVisibility.PUBLIC,
                    false, List.of(), List.of(), new PostStatsResponse(0, 0, 0, 0),
                    Instant.now(), Instant.now(), true, true);
            var page = new PagedResponse<>(List.of(post), 0, 20, 1, 1, true);
            when(bookmarkService.getBookmarkedPosts(eq(1L), any())).thenReturn(page);

            mockMvc.perform(get("/api/bookmarks")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(10));
        }
    }
}
