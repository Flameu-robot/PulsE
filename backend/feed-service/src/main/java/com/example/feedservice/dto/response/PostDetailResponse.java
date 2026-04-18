package com.example.feedservice.dto.response;

import com.example.feedservice.entity.enums.PostVisibility;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record PostDetailResponse(
        Long id,
        Long authorId,
        String content,
        PostVisibility visibility,
        boolean pinned,
        Map<String, Object> metadata,
        List<AttachmentResponse> attachments,
        List<MusicLinkResponse> musicLinks,
        List<Long> groupIds,
        PostStatsResponse stats,
        Instant createdAt,
        Instant updatedAt,
        boolean likedByMe,
        boolean bookmarkedByMe
) {}
