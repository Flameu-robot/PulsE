package com.example.feedservice.dto.response;

import com.example.feedservice.entity.enums.AttachmentType;

import java.util.Map;

public record AttachmentResponse(
        Long id,
        AttachmentType mediaType,
        String url,
        String previewUrl,
        int orderIndex,
        Map<String, Object> metadata
) {}