package com.example.feedservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.feedservice.dto.request.CreateCommentRequest;
import com.example.feedservice.dto.response.CommentResponse;
import com.example.feedservice.dto.response.PagedResponse;
import com.example.feedservice.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Nested
    @DisplayName("POST /api/posts/{postId}/comments")
    class AddComment {

        @Test
        @DisplayName("should return 201 on successful comment")
        void shouldReturn201() throws Exception {
            var response = new CommentResponse(1L, 10L, 1L, "Great post!", null, 0, Instant.now());
            when(commentService.addComment(eq(10L), eq(1L), any())).thenReturn(response);

            var request = new CreateCommentRequest("Great post!", null);

            mockMvc.perform(post("/api/posts/10/comments")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.content").value("Great post!"));
        }

        @Test
        @DisplayName("should return 422 on blank content")
        void shouldReturn422OnBlankContent() throws Exception {
            var request = new CreateCommentRequest("", null);

            mockMvc.perform(post("/api/posts/10/comments")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    @Nested
    @DisplayName("GET /api/posts/{postId}/comments")
    class GetComments {

        @Test
        @DisplayName("should return 200 with comments list")
        void shouldReturn200() throws Exception {
            var page = new PagedResponse<>(List.<CommentResponse>of(), 0, 20, 0, 0, true);
            when(commentService.getRootComments(eq(10L), any())).thenReturn(page);

            mockMvc.perform(get("/api/posts/10/comments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("DELETE /api/comments/{commentId}")
    class DeleteComment {

        @Test
        @DisplayName("should return 204 on successful delete")
        void shouldReturn204() throws Exception {
            mockMvc.perform(delete("/api/comments/5")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isNoContent());

            verify(commentService).deleteComment(5L, 1L);
        }
    }
}
