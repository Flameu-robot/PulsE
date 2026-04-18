package com.example.identityservice.dto.response;

import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        String username,
        String displayName,
        String email,
        String phone,
        String role,
        String status,
        String avatarUrl,
        String bio,
        OffsetDateTime createdAt,
        OffsetDateTime lastLoginAt
) {}
