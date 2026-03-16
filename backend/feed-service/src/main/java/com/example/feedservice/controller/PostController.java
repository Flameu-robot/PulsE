package com.example.feedservice.controller;

import com.example.feedservice.dto.request.CreatePostRequest;
import com.example.feedservice.dto.request.UpdatePostRequest;
import com.example.feedservice.dto.response.PostDetailResponse;
import com.example.feedservice.dto.response.PostResponse;
import com.example.feedservice.security.CurrentUserId;
import com.example.feedservice.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @CurrentUserId Long userId,
            @Valid @RequestBody CreatePostRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(userId, request));
    }

    @GetMapping("/{postId}")
    public PostDetailResponse getPost(
            @PathVariable Long postId,
            @CurrentUserId Long userId) {

        return postService.getPostDetail(postId, userId);
    }

    @PutMapping("/{postId}")
    public PostResponse updatePost(
            @PathVariable Long postId,
            @CurrentUserId Long userId,
            @Valid @RequestBody UpdatePostRequest request) {

        return postService.updatePost(postId, userId, request);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(
            @PathVariable Long postId,
            @CurrentUserId Long userId) {

        postService.deletePost(postId, userId);
    }
}
