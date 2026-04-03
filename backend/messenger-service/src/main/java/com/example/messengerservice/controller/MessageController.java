package com.example.messengerservice.controller;

import com.example.messengerservice.dto.request.MessageRequest;
import com.example.messengerservice.service.MessageService;
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
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<?> sendMessage(
            @Valid @RequestBody MessageRequest req,
            @AuthenticationPrincipal GatewayPrincipal principal) {

        messageService.sendMessage(principal.getUserId(), req);

        return ResponseEntity.ok().build();
    }
}
