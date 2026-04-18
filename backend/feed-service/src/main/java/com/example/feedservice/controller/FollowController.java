package com.example.feedservice.controller;

import com.example.feedservice.dto.response.FollowResponse;
import com.example.feedservice.dto.response.PagedResponse;
import com.example.feedservice.security.CurrentUserId;
import com.example.feedservice.service.FollowService;
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
@RequestMapping("/api/users/{userId}")
@RequiredArgsConstructor
@Tag(name = "Подписки", description = "Подписки на пользователей")
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "Подписаться на пользователя")
    @PostMapping("/follow")
    public ResponseEntity<FollowResponse> follow(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long userId,
            @Parameter(hidden = true)
            @CurrentUserId Long currentUserId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(followService.follow(currentUserId, userId));
    }

    @Operation(summary = "Отписаться от пользователя")
    @DeleteMapping("/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long userId,
            @Parameter(hidden = true)
            @CurrentUserId Long currentUserId) {

        followService.unfollow(currentUserId, userId);
    }

    @Operation(summary = "Проверить, подписан ли на пользователя")
    @GetMapping("/follow/status")
    public Map<String, Boolean> checkFollowing(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long userId,
            @Parameter(hidden = true)
            @CurrentUserId Long currentUserId) {

        return Map.of("following", followService.isFollowing(currentUserId, userId));
    }

    @Operation(summary = "Получить подписчиков пользователя")
    @GetMapping("/followers")
    public PagedResponse<FollowResponse> getFollowers(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long userId,
            @Parameter(description = "Номер страницы (с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @PageableDefault(size = 20) Pageable pageable) {

        return followService.getFollowers(userId, withoutSort(pageable));
    }

    @Operation(summary = "Получить подписки пользователя")
    @GetMapping("/following")
    public PagedResponse<FollowResponse> getFollowing(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long userId,
            @Parameter(description = "Номер страницы (с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @PageableDefault(size = 20) Pageable pageable) {

        return followService.getFollowing(userId, withoutSort(pageable));
    }

    @Operation(summary = "Получить количество подписчиков и подписок")
    @GetMapping("/follow/counts")
    public Map<String, Long> getCounts(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long userId) {
        return Map.of(
                "followers", followService.getFollowersCount(userId),
                "following", followService.getFollowingCount(userId)
        );
    }
}