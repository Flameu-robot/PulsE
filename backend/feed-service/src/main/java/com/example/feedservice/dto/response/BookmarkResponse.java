package com.example.feedservice.dto.response;

import java.time.Instant;

public record BookmarkResponse(
        Long id,
        Long postId,
        Instant createdAt
) {}
