package com.example.messengerservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.AssertTrue;

public record MessageRequest(
        Long groupId,
        Long channelId,

        Long targetUserId,
        @NotBlank String text
) {
    @AssertTrue(message = "For group messages, both groupId and channelId must be provided. For DMs, only targetUserId.")
    public boolean isValidTarget() {
        boolean isGroup = (groupId != null && channelId != null && targetUserId == null);
        boolean isDirect = (groupId == null && channelId == null && targetUserId != null);
        return isGroup || isDirect;
    }
}