package com.example.identityservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Смена пароля")
public record ChangePasswordRequest(

        @Schema(description = "Текущий пароль", example = "Password123!")
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @Schema(description = "Новый пароль (мин. 8 символов)", example = "NewPassword456!")
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword
) {}
