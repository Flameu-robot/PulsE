package com.example.feedservice.dto.request;

import com.example.feedservice.entity.enums.AttachmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record AttachmentRequest(

        @NotNull(message = "Media type is required")
        AttachmentType mediaType,

        @NotBlank(message = "URL is required")
        @Size(max = 500)
        String url,

        @Size(max = 500)
        String previewUrl,

        int orderIndex,

        Map<String, Object> metadata
) {}