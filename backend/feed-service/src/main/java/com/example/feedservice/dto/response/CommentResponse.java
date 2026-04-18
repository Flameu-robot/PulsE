package com.example.feedservice.dto.response;

import java.time.Instant;

public record CommentResponse(
        Long id,
        Long postId,
        Long authorId,
        String content,
        Long parentCommentId,
        long replyCount,
        Instant createdAt
) {}
