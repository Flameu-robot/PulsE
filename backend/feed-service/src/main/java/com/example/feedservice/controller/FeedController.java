package com.example.feedservice.controller;

import com.example.feedservice.dto.response.PagedResponse;
import com.example.feedservice.dto.response.PostResponse;
import com.example.feedservice.security.CurrentUserId;
import com.example.feedservice.service.FeedService;
import com.example.feedservice.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final PostService postService;

    @GetMapping("/api/feed/following")
    public PagedResponse<PostResponse> getFollowingFeed(
            @CurrentUserId Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return feedService.getFollowingFeed(userId, pageable);
    }

    @GetMapping("/api/feed/explore")
    public PagedResponse<PostResponse> getExploreFeed(
            @CurrentUserId Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return feedService.getExploreFeed(userId, pageable);
    }

    @GetMapping("/api/users/{authorId}/posts")
    public PagedResponse<PostResponse> getUserPosts(
            @PathVariable Long authorId,
            @CurrentUserId Long currentUserId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return postService.getUserPosts(authorId, currentUserId, pageable);
    }
}
