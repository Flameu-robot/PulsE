package com.example.feedservice.controller;

import com.example.feedservice.dto.response.PagedResponse;
import com.example.feedservice.dto.response.PostResponse;
import com.example.feedservice.security.CurrentUserId;
import com.example.feedservice.service.FeedService;
import com.example.feedservice.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import static com.example.feedservice.util.PageableUtils.withoutSort;

@RestController
@RequiredArgsConstructor
@Tag(name = "Лента", description = "Лента постов")
public class FeedController {

    private final FeedService feedService;
    private final PostService postService;

    @Operation(summary = "Лента подписок (посты от тех, на кого подписан)")
    @GetMapping("/api/feed/following")
    public PagedResponse<PostResponse> getFollowingFeed(
            @Parameter(hidden = true)
            @CurrentUserId Long userId,
            @Parameter(description = "Номер страницы (с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return feedService.getFollowingFeed(userId, withoutSort(pageable));
    }

    @Operation(summary = "Лента рекомендаций (интересные посты)")
    @GetMapping("/api/feed/explore")
    public PagedResponse<PostResponse> getExploreFeed(
            @Parameter(hidden = true)
            @CurrentUserId Long userId,
            @Parameter(description = "Номер страницы (с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return feedService.getExploreFeed(userId, withoutSort(pageable));
    }

    @Operation(summary = "Лента групп (посты из групп пользователя)")
    @GetMapping("/api/feed/groups")
    public PagedResponse<PostResponse> getGroupsFeed(
            @Parameter(hidden = true)
            @CurrentUserId Long userId,
            @Parameter(description = "Номер страницы (с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return feedService.getGroupsFeed(userId, withoutSort(pageable));
    }

    @Operation(summary = "Посты конкретного пользователя")
    @GetMapping("/api/users/{authorId}/posts")
    public PagedResponse<PostResponse> getUserPosts(
            @Parameter(description = "ID автора", example = "1")
            @PathVariable Long authorId,
            @Parameter(hidden = true)
            @CurrentUserId Long currentUserId,
            @Parameter(description = "Номер страницы (с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return postService.getUserPosts(authorId, currentUserId, withoutSort(pageable));
    }

    @Operation(summary = "Посты конкретной группы")
    @GetMapping("/api/groups/{groupId}/posts")
    public PagedResponse<PostResponse> getGroupPosts(
            @Parameter(description = "ID группы", example = "1")
            @PathVariable Long groupId,
            @Parameter(hidden = true)
            @CurrentUserId Long currentUserId,
            @Parameter(description = "Номер страницы (с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return postService.getGroupPosts(groupId, currentUserId, withoutSort(pageable));
    }
}