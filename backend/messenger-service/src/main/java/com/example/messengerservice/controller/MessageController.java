package com.example.messengerservice.controller;

import com.example.messengerservice.dto.request.MessageRequest;
import com.example.messengerservice.dto.response.MessageResponse;
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
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody MessageRequest request,
            @AuthenticationPrincipal GatewayPrincipal principal) {

        MessageResponse response = messageService.sendMessage(principal.getUserId(), request);

        return ResponseEntity.ok(response);
    }
}
