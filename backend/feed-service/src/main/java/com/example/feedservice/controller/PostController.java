package com.example.feedservice.controller;

import com.example.feedservice.dto.request.CreatePostRequest;
import com.example.feedservice.dto.request.UpdatePostRequest;
import com.example.feedservice.dto.response.PostDetailResponse;
import com.example.feedservice.dto.response.PostResponse;
import com.example.feedservice.security.CurrentUserId;
import com.example.feedservice.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Посты", description = "Создание и управление постами")
public class PostController {

    private final PostService postService;

    @Operation(summary = "Создать новый пост")
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Parameter(hidden = true)
            @CurrentUserId Long userId,
            @Valid @RequestBody CreatePostRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(userId, request));
    }

    @Operation(summary = "Получить пост по ID")
    @GetMapping("/{postId}")
    public PostDetailResponse getPost(
            @Parameter(description = "ID поста", example = "1")
            @PathVariable Long postId,
            @Parameter(hidden = true)
            @CurrentUserId Long userId) {

        return postService.getPostDetail(postId, userId);
    }

    @Operation(summary = "Обновить пост")
    @PutMapping("/{postId}")
    public PostResponse updatePost(
            @Parameter(description = "ID поста", example = "1")
            @PathVariable Long postId,
            @Parameter(hidden = true)
            @CurrentUserId Long userId,
            @Valid @RequestBody UpdatePostRequest request) {

        return postService.updatePost(postId, userId, request);
    }

    @Operation(summary = "Удалить пост")
    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(
            @Parameter(description = "ID поста", example = "1")
            @PathVariable Long postId,
            @Parameter(hidden = true)
            @CurrentUserId Long userId) {

        postService.deletePost(postId, userId);
    }

    @Operation(summary = "Отметить пост как просмотренный")
    @PostMapping("/api/posts/{postId}/view")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recordView(
            @PathVariable Long postId,
            @Parameter(hidden = true) @CurrentUserId Long userId
    ) {
        postService.recordView(postId, userId);
    }
}