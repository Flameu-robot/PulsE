package com.example.feedservice.dto.response;

import com.example.feedservice.entity.enums.FollowStatus;

import java.time.Instant;

public record FollowResponse(
        Long id,
        Long followerId,
        Long followeeId,
        FollowStatus status,
        Instant createdAt
) {}