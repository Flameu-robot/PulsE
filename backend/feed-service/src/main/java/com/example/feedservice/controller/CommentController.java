package com.example.feedservice.controller;

import com.example.feedservice.dto.request.CreateCommentRequest;
import com.example.feedservice.dto.response.CommentResponse;
import com.example.feedservice.dto.response.PagedResponse;
import com.example.feedservice.security.CurrentUserId;
import com.example.feedservice.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Комментарии", description = "Комментарии к постам")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Добавить комментарий к посту")
    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @Parameter(description = "ID поста", example = "1")
            @PathVariable Long postId,
            @Parameter(hidden = true)
            @CurrentUserId Long userId,
            @Valid @RequestBody CreateCommentRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(postId, userId, request));
    }

    @Operation(summary = "Получить комментарии к посту")
    @GetMapping("/api/posts/{postId}/comments")
    public PagedResponse<CommentResponse> getComments(
            @Parameter(description = "ID поста", example = "1")
            @PathVariable Long postId,
            @Parameter(description = "Номер страницы (с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @PageableDefault(size = 20) Pageable pageable) {

        return commentService.getRootComments(postId, withoutSort(pageable));
    }

    @Operation(summary = "Получить ответы на комментарий")
    @GetMapping("/api/comments/{commentId}/replies")
    public PagedResponse<CommentResponse> getReplies(
            @Parameter(description = "ID комментария", example = "1")
            @PathVariable Long commentId,
            @Parameter(description = "Номер страницы (с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @PageableDefault(size = 10) Pageable pageable) {

        return commentService.getReplies(commentId, withoutSort(pageable));
    }

    @Operation(summary = "Удалить комментарий")
    @DeleteMapping("/api/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @Parameter(description = "ID комментария", example = "1")
            @PathVariable Long commentId,
            @Parameter(hidden = true)
            @CurrentUserId Long userId) {

        commentService.deleteComment(commentId, userId);
    }
}