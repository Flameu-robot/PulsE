package com.example.feedservice.dto.response;

public record MusicLinkResponse(
        Long id,
        Long trackId,
        int listenCount
) {}
