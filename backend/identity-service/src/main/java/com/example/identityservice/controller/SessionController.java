package com.example.identityservice.controller;

import com.example.identityservice.dto.response.SessionResponse;
import com.example.identityservice.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/sessions")
@Tag(name = "Сессии", description = "Активные сессии пользователя")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Operation(summary = "Список активных сессий")
    @GetMapping
    public ResponseEntity<List<SessionResponse>> getActiveSessions(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<SessionResponse> sessions = sessionService.getActiveSessions(
                userDetails.getUsername()
        );
        return ResponseEntity.ok(sessions);
    }

    @Operation(summary = "Завершить сессию по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeSession(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID сессии", example = "1")
            @PathVariable Long id
    ) {
        sessionService.revokeSession(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}