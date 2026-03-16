package com.example.feedservice.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(

        @NotBlank(message = "Comment content is required")
        @Size(max = 2000, message = "Comment must be at most 2000 characters")
        String content,

        Long parentCommentId
) {}