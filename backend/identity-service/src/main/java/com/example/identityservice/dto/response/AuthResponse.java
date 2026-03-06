package com.example.identityservice.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String username,
        String role
) {}
