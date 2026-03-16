package com.example.feedservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.feedservice.config.PostgresTestContainerConfig;
import com.example.feedservice.dto.request.CreatePostRequest;
import com.example.feedservice.dto.request.UpdatePostRequest;
import com.example.feedservice.dto.response.*;
import com.example.feedservice.entity.enums.PostVisibility;
import com.example.feedservice.service.PostService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostControllerTest extends PostgresTestContainerConfig {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    private ObjectMapper objectMapper;

    private final PostResponse sampleResponse = new PostResponse(
            1L, 1L, "Hello", PostVisibility.PUBLIC,
            false, List.of(), List.of(), new PostStatsResponse(0, 0, 0, 0),
            Instant.now(), Instant.now(), false, false
    );

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Nested
    @DisplayName("POST /api/posts")
    class CreatePost {

        @Test
        @DisplayName("should return 201 on successful creation")
        void shouldReturn201OnSuccess() throws Exception {
            when(postService.createPost(eq(1L), any())).thenReturn(sampleResponse);

            var request = new CreatePostRequest("Hello", PostVisibility.PUBLIC, null, null);

            mockMvc.perform(post("/api/posts")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.content").value("Hello"));
        }

        @Test
        @DisplayName("should return 422 on blank content")
        void shouldReturn422OnBlankContent() throws Exception {
            var request = new CreatePostRequest("", PostVisibility.PUBLIC, null, null);

            mockMvc.perform(post("/api/posts")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    @Nested
    @DisplayName("GET /api/posts/{postId}")
    class GetPost {

        @Test
        @DisplayName("should return 200 with post detail")
        void shouldReturn200() throws Exception {
            var detail = new PostDetailResponse(
                    1L, 1L, "Hello", PostVisibility.PUBLIC,
                    false, Map.of(), List.of(), List.of(),
                    new PostStatsResponse(0, 0, 0, 0),
                    Instant.now(), Instant.now(), false, false
            );
            when(postService.getPostDetail(eq(1L), any())).thenReturn(detail);

            mockMvc.perform(get("/api/posts/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }
    }

    @Nested
    @DisplayName("PUT /api/posts/{postId}")
    class UpdatePost {

        @Test
        @DisplayName("should return 200 on successful update")
        void shouldReturn200() throws Exception {
            when(postService.updatePost(eq(1L), eq(1L), any())).thenReturn(sampleResponse);

            var request = new UpdatePostRequest("Updated", null, null);

            mockMvc.perform(put("/api/posts/1")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/posts/{postId}")
    class DeletePost {

        @Test
        @DisplayName("should return 204 on successful delete")
        void shouldReturn204() throws Exception {
            mockMvc.perform(delete("/api/posts/1")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isNoContent());

            verify(postService).deletePost(1L, 1L);
        }
    }
}
