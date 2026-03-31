package com.example.feedservice.controller;

import com.example.feedservice.security.CurrentUserId;
import com.example.feedservice.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
@RequiredArgsConstructor
@Tag(name = "Лайки", description = "Лайки постов")
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "Поставить лайк посту")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void like(
            @Parameter(description = "ID поста", example = "1")
            @PathVariable Long postId,
            @Parameter(hidden = true)
            @CurrentUserId Long userId) {

        likeService.likePost(postId, userId);
    }

    @Operation(summary = "Убрать лайк с поста")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(
            @Parameter(description = "ID поста", example = "1")
            @PathVariable Long postId,
            @Parameter(hidden = true)
            @CurrentUserId Long userId) {

        likeService.unlikePost(postId, userId);
    }

    @Operation(summary = "Проверить, поставлен ли лайк")
    @GetMapping("/status")
    public Map<String, Boolean> checkLike(
            @Parameter(description = "ID поста", example = "1")
            @PathVariable Long postId,
            @Parameter(hidden = true)
            @CurrentUserId Long userId) {

        return Map.of("liked", likeService.isLiked(postId, userId));
    }
}