package com.example.feedservice.controller;

import com.example.feedservice.config.PostgresTestContainerConfig;
import com.example.feedservice.service.LikeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LikeControllerTest extends PostgresTestContainerConfig {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LikeService likeService;

    @Nested
    @DisplayName("POST /api/posts/{postId}/likes")
    class Like {

        @Test
        @DisplayName("should return 201 on successful like")
        void shouldReturn201() throws Exception {
            mockMvc.perform(post("/api/posts/10/likes")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isCreated());

            verify(likeService).likePost(10L, 1L);
        }
    }

    @Nested
    @DisplayName("DELETE /api/posts/{postId}/likes")
    class Unlike {

        @Test
        @DisplayName("should return 204 on successful unlike")
        void shouldReturn204() throws Exception {
            mockMvc.perform(delete("/api/posts/10/likes")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isNoContent());

            verify(likeService).unlikePost(10L, 1L);
        }
    }

    @Nested
    @DisplayName("GET /api/posts/{postId}/likes/status")
    class CheckStatus {

        @Test
        @DisplayName("should return 200 with like status")
        void shouldReturn200() throws Exception {
            when(likeService.isLiked(10L, 1L)).thenReturn(true);

            mockMvc.perform(get("/api/posts/10/likes/status")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.liked").value(true));
        }
    }
}
