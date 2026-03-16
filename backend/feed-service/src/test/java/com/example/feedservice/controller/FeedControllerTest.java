package com.example.feedservice.controller;

import com.example.feedservice.dto.response.*;
import com.example.feedservice.entity.enums.PostVisibility;
import com.example.feedservice.service.FeedService;
import com.example.feedservice.service.PostService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedService feedService;

    @MockitoBean
    private PostService postService;

    private PagedResponse<PostResponse> samplePage() {
        var post = new PostResponse(1L, 2L, "Test", PostVisibility.PUBLIC,
                false, List.of(), List.of(), new PostStatsResponse(0, 0, 0, 0),
                Instant.now(), Instant.now(), false, false);
        return new PagedResponse<>(List.of(post), 0, 20, 1, 1, true);
    }

    @Nested
    @DisplayName("GET /api/feed/following")
    class FollowingFeed {

        @Test
        @DisplayName("should return 200 with feed for authenticated user")
        void shouldReturn200() throws Exception {
            when(feedService.getFollowingFeed(eq(1L), any())).thenReturn(samplePage());

            mockMvc.perform(get("/api/feed/following")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/feed/explore")
    class ExploreFeed {

        @Test
        @DisplayName("should return 200 for anonymous user")
        void shouldReturn200Anonymous() throws Exception {
            when(feedService.getExploreFeed(any(), any())).thenReturn(samplePage());

            mockMvc.perform(get("/api/feed/explore"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/users/{authorId}/posts")
    class UserPosts {

        @Test
        @DisplayName("should return 200 with user posts")
        void shouldReturn200() throws Exception {
            when(postService.getUserPosts(eq(42L), any(), any())).thenReturn(samplePage());

            mockMvc.perform(get("/api/users/42/posts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }
}