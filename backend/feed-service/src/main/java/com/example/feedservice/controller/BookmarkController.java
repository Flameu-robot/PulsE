package com.example.feedservice.controller;

import com.example.feedservice.dto.response.BookmarkResponse;
import com.example.feedservice.dto.response.PagedResponse;
import com.example.feedservice.dto.response.PostResponse;
import com.example.feedservice.security.CurrentUserId;
import com.example.feedservice.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.example.feedservice.util.PageableUtils.withoutSort;

@RestController
@RequiredArgsConstructor
@Tag(name = "Закладки", description = "Сохранение постов в закладки")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(summary = "Добавить пост в закладки")
    @PostMapping("/api/posts/{postId}/bookmarks")
    public ResponseEntity<BookmarkResponse> addBookmark(
            @Parameter(description = "ID поста", example = "1")
            @PathVariable Long postId,
            @Parameter(hidden = true)
            @CurrentUserId Long userId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookmarkService.addBookmark(userId, postId));
    }

    @Operation(summary = "Удалить пост из закладок")
    @DeleteMapping("/api/posts/{postId}/bookmarks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBookmark(
            @Parameter(description = "ID поста", example = "1")
            @PathVariable Long postId,
            @Parameter(hidden = true)
            @CurrentUserId Long userId) {

        bookmarkService.removeBookmark(userId, postId);
    }

    @Operation(summary = "Проверить, добавлен ли пост в закладки")
    @GetMapping("/api/posts/{postId}/bookmarks/status")
    public Map<String, Boolean> checkBookmark(
            @Parameter(description = "ID поста", example = "1")
            @PathVariable Long postId,
            @Parameter(hidden = true)
            @CurrentUserId Long userId) {

        return Map.of("bookmarked", bookmarkService.isBookmarked(userId, postId));
    }

    @Operation(summary = "Получить мои закладки")
    @GetMapping("/api/bookmarks")
    public PagedResponse<PostResponse> getMyBookmarks(
            @Parameter(hidden = true)
            @CurrentUserId Long userId,
            @Parameter(description = "Номер страницы (с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @PageableDefault(size = 20) Pageable pageable) {

        return bookmarkService.getBookmarkedPosts(userId, withoutSort(pageable));
    }
}