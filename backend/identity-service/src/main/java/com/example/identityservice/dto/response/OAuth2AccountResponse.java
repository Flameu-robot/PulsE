package com.example.identityservice.dto.response;

import java.time.OffsetDateTime;

public record OAuth2AccountResponse(
        Long id,
        String provider,
        String providerId,
        OffsetDateTime linkedAt
) {}
