package com.example.identityservice.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 500, message = "Bio must be at most 500 characters")
        String bio,

        @Size(max = 20, message = "Phone must be at most 20 characters")
        String phone,

        @Size(max = 500, message = "Avatar URL must be at most 500 characters")
        String avatarUrl
) {}
