package com.example.feedservice.controller;

import com.example.feedservice.dto.response.FollowResponse;
import com.example.feedservice.dto.response.PagedResponse;
import com.example.feedservice.security.CurrentUserId;
import com.example.feedservice.service.FollowService;
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
public class FollowController {

    private final FollowService followService;

    @PostMapping("/follow")
    public ResponseEntity<FollowResponse> follow(
            @PathVariable Long userId,
            @CurrentUserId Long currentUserId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(followService.follow(currentUserId, userId));
    }

    @DeleteMapping("/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(
            @PathVariable Long userId,
            @CurrentUserId Long currentUserId) {

        followService.unfollow(currentUserId, userId);
    }

    @GetMapping("/follow/status")
    public Map<String, Boolean> checkFollowing(
            @PathVariable Long userId,
            @CurrentUserId Long currentUserId) {

        return Map.of("following", followService.isFollowing(currentUserId, userId));
    }

    @GetMapping("/followers")
    public PagedResponse<FollowResponse> getFollowers(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {

        return followService.getFollowers(userId,  withoutSort(pageable));
    }

    @GetMapping("/following")
    public PagedResponse<FollowResponse> getFollowing(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {

        return followService.getFollowing(userId, withoutSort(pageable));
    }

    @GetMapping("/follow/counts")
    public Map<String, Long> getCounts(@PathVariable Long userId) {
        return Map.of(
                "followers", followService.getFollowersCount(userId),
                "following", followService.getFollowingCount(userId)
        );
    }
}
