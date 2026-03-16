package com.example.feedservice.dto.response;

import com.example.feedservice.entity.enums.PostVisibility;

import java.time.Instant;
import java.util.List;

public record PostResponse(
        Long id,
        Long authorId,
        String content,
        PostVisibility visibility,
        boolean pinned,
        List<AttachmentResponse> attachments,
        List<Long> trackIds,
        PostStatsResponse stats,
        Instant createdAt,
        Instant updatedAt,
        boolean likedByMe,
        boolean bookmarkedByMe
) {}
