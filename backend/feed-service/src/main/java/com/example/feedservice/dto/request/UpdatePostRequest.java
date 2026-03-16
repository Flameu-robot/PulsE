package com.example.feedservice.dto.request;

import com.example.feedservice.entity.enums.PostVisibility;
import jakarta.validation.constraints.Size;

public record UpdatePostRequest(

        @Size(max = 5000, message = "Content must be at most 5000 characters")
        String content,

        PostVisibility visibility,

        Boolean pinned
) {}
