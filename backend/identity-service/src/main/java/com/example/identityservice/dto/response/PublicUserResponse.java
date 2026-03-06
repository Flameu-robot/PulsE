package com.example.identityservice.dto.response;

import java.time.OffsetDateTime;

public record PublicUserResponse(
        Long id,
        String username,
        String avatarUrl,
        String bio,
        OffsetDateTime createdAt
) {}
