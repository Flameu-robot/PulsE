package com.example.messengerservice.controller;

import com.example.messengerservice.dto.request.GroupRequest;
import com.example.messengerservice.service.GroupService;
import com.example.shared.security.GatewayPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<Void> createGroup(
            @Valid @RequestBody GroupRequest req,
            @AuthenticationPrincipal GatewayPrincipal principal
            ) {

        groupService.createGroup(principal.getUserId(), req);

        return ResponseEntity.noContent().build();
    }
}
