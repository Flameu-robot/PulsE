package com.example.feedservice.dto.request;


import com.example.feedservice.entity.enums.PostVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePostRequest(

        @NotBlank(message = "Content is required")
        @Size(max = 5000, message = "Content must be at most 5000 characters")
        String content,

        PostVisibility visibility,

        @Valid
        @Size(max = 10, message = "Maximum 10 attachments")
        List<AttachmentRequest> attachments,

        @Size(max = 5, message = "Maximum 5 tracks")
        List<Long> trackIds,

        @Size(max = 10, message = "Maximum 10 groups")
        List<Long> groupIds
) {
    public CreatePostRequest {
        if (visibility == null) visibility = PostVisibility.PUBLIC;
        if (groupIds == null) groupIds = List.of();
        if (attachments == null) attachments = List.of();
        if (trackIds == null) trackIds = List.of();
    }
}
