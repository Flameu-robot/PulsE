package com.example.identityservice.dto.response;

import java.time.OffsetDateTime;

public record SessionResponse(
        Long id,
        String userAgent,
        String ipAddress,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt
) {}
