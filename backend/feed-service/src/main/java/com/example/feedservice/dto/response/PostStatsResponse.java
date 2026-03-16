package com.example.feedservice.dto.response;

public record PostStatsResponse(
        int likesCount,
        int commentsCount,
        int sharesCount,
        int viewsCount
) {}
