package com.example.messengerservice.dto.request;

import com.example.messengerservice.entity.enums.GroupType;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GroupRequest (
        @NotNull
        GroupType type, // GROUP or SERVER

        @NotNull
        String name,

        List<Long> initialMembers
) {}
