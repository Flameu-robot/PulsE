package com.example.messengerservice.dto.request;

import java.util.List;

public record GroupRequest (
        String name,
        List<Long> initialMembers
) {}
