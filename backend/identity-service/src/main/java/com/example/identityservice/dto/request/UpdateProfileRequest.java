package com.example.identityservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;


@Schema(description = "Обновление профиля")
public record UpdateProfileRequest(

        @Schema(description = "О себе", example = "Я сумасшедший")
        @Size(max = 500, message = "Bio must be at most 500 characters")
        String bio,

        @Schema(description = "Номер телефона", example = "79131065267")
        @Size(max = 20, message = "Phone must be at most 20 characters")
        String phone,

        @Schema(description = "URL аватара", example = "https://example.com/avatar.jpg")
        @Size(max = 500, message = "Avatar URL must be at most 500 characters")
        String avatarUrl
) {}
