package com.example.messengerservice.dto.response;

import java.time.OffsetDateTime;

public record MessageResponse(
        Long messageId,
        Long groupId,
        Long channelId,
        Long authorId,
        String content,
        OffsetDateTime createdAt
) {}