package com.example.feedservice.controller;

import com.example.feedservice.dto.request.CreateCommentRequest;
import com.example.feedservice.dto.response.CommentResponse;
import com.example.feedservice.dto.response.PagedResponse;
import com.example.feedservice.security.CurrentUserId;
import com.example.feedservice.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.feedservice.util.PageableUtils.withoutSort;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long postId,
            @CurrentUserId Long userId,
            @Valid @RequestBody CreateCommentRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(postId, userId, request));
    }

    @GetMapping("/api/posts/{postId}/comments")
    public PagedResponse<CommentResponse> getComments(
            @PathVariable Long postId,
            @PageableDefault(size = 20) Pageable pageable) {

        return commentService.getRootComments(postId, withoutSort(pageable));
    }

    @GetMapping("/api/comments/{commentId}/replies")
    public PagedResponse<CommentResponse> getReplies(
            @PathVariable Long commentId,
            @PageableDefault(size = 10) Pageable pageable) {

        return commentService.getReplies(commentId, withoutSort(pageable));
    }

    @DeleteMapping("/api/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long commentId,
            @CurrentUserId Long userId) {

        commentService.deleteComment(commentId, userId);
    }
}
