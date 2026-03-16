package com.example.feedservice.controller;

import com.example.feedservice.security.CurrentUserId;
import com.example.feedservice.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void like(
            @PathVariable Long postId,
            @CurrentUserId Long userId) {

        likeService.likePost(postId, userId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(
            @PathVariable Long postId,
            @CurrentUserId Long userId) {

        likeService.unlikePost(postId, userId);
    }

    @GetMapping("/status")
    public Map<String, Boolean> checkLike(
            @PathVariable Long postId,
            @CurrentUserId Long userId) {

        return Map.of("liked", likeService.isLiked(postId, userId));
    }
}
