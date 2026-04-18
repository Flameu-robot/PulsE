package com.example.identityservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


@Schema(description = "Запрос сброса пароля")
public record ForgotPasswordRequest(

        @Schema(description = "Email аккаунта", example = "test@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {}