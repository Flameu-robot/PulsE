package com.example.identityservice.controller;

import com.example.identityservice.dto.response.SessionResponse;
import com.example.identityservice.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public ResponseEntity<List<SessionResponse>> getActiveSessions(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<SessionResponse> sessions = sessionService.getActiveSessions(
                userDetails.getUsername()
        );
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        sessionService.revokeSession(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
