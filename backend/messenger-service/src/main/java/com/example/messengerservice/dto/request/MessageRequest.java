package com.example.messengerservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.AssertTrue;

public record MessageRequest(
        Long groupId,
        Long targetUserId,
        @NotBlank String text
) {
    // Должно быть заполнено только одно из двух полей
    @AssertTrue(message = "Specify either groupId OR targetUserId, not both")
    public boolean isValidTarget() {
        return (groupId != null && targetUserId == null) ||
                (groupId == null && targetUserId != null);
    }
}