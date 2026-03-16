package com.example.feedservice.controller;

import com.example.feedservice.dto.response.BookmarkResponse;
import com.example.feedservice.dto.response.PagedResponse;
import com.example.feedservice.dto.response.PostResponse;
import com.example.feedservice.security.CurrentUserId;
import com.example.feedservice.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/api/posts/{postId}/bookmarks")
    public ResponseEntity<BookmarkResponse> addBookmark(
            @PathVariable Long postId,
            @CurrentUserId Long userId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookmarkService.addBookmark(userId, postId));
    }

    @DeleteMapping("/api/posts/{postId}/bookmarks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBookmark(
            @PathVariable Long postId,
            @CurrentUserId Long userId) {

        bookmarkService.removeBookmark(userId, postId);
    }

    @GetMapping("/api/posts/{postId}/bookmarks/status")
    public Map<String, Boolean> checkBookmark(
            @PathVariable Long postId,
            @CurrentUserId Long userId) {

        return Map.of("bookmarked", bookmarkService.isBookmarked(userId, postId));
    }

    @GetMapping("/api/bookmarks")
    public PagedResponse<PostResponse> getMyBookmarks(
            @CurrentUserId Long userId,
            @PageableDefault(size = 20) Pageable pageable) {

        return bookmarkService.getBookmarkedPosts(userId, pageable);
    }
}
