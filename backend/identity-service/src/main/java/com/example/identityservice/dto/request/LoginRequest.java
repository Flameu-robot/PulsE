package com.example.identityservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Вход")
public record LoginRequest(

        @Schema(description = "Email", example = "test@example.com")
        @NotBlank(message = "Username or email is required")
        String login,

        @Schema(description = "Пароль", example = "Password123!")
        @NotBlank(message = "Password is required")
        String password
) {}