package com.example.feedservice.controller;

import com.example.feedservice.dto.response.FollowResponse;
import com.example.feedservice.dto.response.PagedResponse;
import com.example.feedservice.entity.enums.FollowStatus;
import com.example.feedservice.service.FollowService;
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
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FollowService followService;

    @Nested
    @DisplayName("POST /api/users/{userId}/follow")
    class Follow {

        @Test
        @DisplayName("should return 201 on successful follow")
        void shouldReturn201() throws Exception {
            var response = new FollowResponse(1L, 1L, 42L, FollowStatus.ACTIVE, Instant.now());
            when(followService.follow(1L, 42L)).thenReturn(response);

            mockMvc.perform(post("/api/users/42/follow")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.followeeId").value(42));
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{userId}/follow")
    class Unfollow {

        @Test
        @DisplayName("should return 204 on successful unfollow")
        void shouldReturn204() throws Exception {
            mockMvc.perform(delete("/api/users/42/follow")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isNoContent());

            verify(followService).unfollow(1L, 42L);
        }
    }

    @Nested
    @DisplayName("GET /api/users/{userId}/follow/status")
    class CheckStatus {

        @Test
        @DisplayName("should return 200 with following status")
        void shouldReturn200() throws Exception {
            when(followService.isFollowing(1L, 42L)).thenReturn(true);

            mockMvc.perform(get("/api/users/42/follow/status")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.following").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/users/{userId}/followers")
    class Followers {

        @Test
        @DisplayName("should return 200 with followers list")
        void shouldReturn200() throws Exception {
            var page = new PagedResponse<>(List.<FollowResponse>of(), 0, 20, 0, 0, true);
            when(followService.getFollowers(eq(42L), any())).thenReturn(page);

            mockMvc.perform(get("/api/users/42/followers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/users/{userId}/follow/counts")
    class Counts {

        @Test
        @DisplayName("should return 200 with follower and following counts")
        void shouldReturn200() throws Exception {
            when(followService.getFollowersCount(42L)).thenReturn(100L);
            when(followService.getFollowingCount(42L)).thenReturn(50L);

            mockMvc.perform(get("/api/users/42/follow/counts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.followers").value(100))
                    .andExpect(jsonPath("$.following").value(50));
        }
    }
}
