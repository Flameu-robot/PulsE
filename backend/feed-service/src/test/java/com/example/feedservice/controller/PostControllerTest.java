package com.example.feedservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    private PostResponse createSamplePostResponse() {
        return new PostResponse(
                1L,
                1L,
                "Hello",
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

    private PostResponse createPostResponseWithGroups(List<Long> groupIds) {
        return new PostResponse(
                1L,
                1L,
                "Hello",
                PostVisibility.PUBLIC,
                false,
                List.of(),
                List.of(),
                groupIds,
                new PostStatsResponse(0, 0, 0, 0),
                Instant.now(),
                Instant.now(),
                false,
                false
        );
    }

    private PostDetailResponse createSamplePostDetailResponse() {
        return new PostDetailResponse(
                1L,
                1L,
                "Hello",
                PostVisibility.PUBLIC,
                false,
                Map.of(),
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

    @Nested
    @DisplayName("POST /api/posts")
    class CreatePost {

        @Test
        @DisplayName("should return 201 on successful creation")
        void shouldReturn201OnSuccess() throws Exception {
            when(postService.createPost(eq(1L), any())).thenReturn(createSamplePostResponse());

            var request = new CreatePostRequest(
                    "Hello",
                    PostVisibility.PUBLIC,
                    List.of(),
                    List.of(),
                    List.of()
            );

            mockMvc.perform(post("/api/posts")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.content").value("Hello"));
        }

        @Test
        @DisplayName("should return 201 when posting to groups")
        void shouldReturn201WhenPostingToGroups() throws Exception {
            var responseWithGroups = createPostResponseWithGroups(List.of(123L, 456L));
            when(postService.createPost(eq(1L), any())).thenReturn(responseWithGroups);

            var request = new CreatePostRequest(
                    "Hello",
                    PostVisibility.PUBLIC,
                    List.of(),
                    List.of(),
                    List.of(123L, 456L)
            );

            mockMvc.perform(post("/api/posts")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.groupIds").isArray())
                    .andExpect(jsonPath("$.groupIds[0]").value(123))
                    .andExpect(jsonPath("$.groupIds[1]").value(456));
        }

        @Test
        @DisplayName("should return 422 on blank content")
        void shouldReturn422OnBlankContent() throws Exception {
            var request = new CreatePostRequest(
                    "",
                    PostVisibility.PUBLIC,
                    List.of(),
                    List.of(),
                    List.of()
            );

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
            when(postService.getPostDetail(eq(1L), any())).thenReturn(createSamplePostDetailResponse());

            mockMvc.perform(get("/api/posts/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.groupIds").isArray());
        }
    }

    @Nested
    @DisplayName("PUT /api/posts/{postId}")
    class UpdatePost {

        @Test
        @DisplayName("should return 200 on successful update")
        void shouldReturn200() throws Exception {
            when(postService.updatePost(eq(1L), eq(1L), any())).thenReturn(createSamplePostResponse());

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