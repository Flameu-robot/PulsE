package com.example.identityservice.controller;

import com.example.identityservice.dto.response.AuthResponse;
import com.example.identityservice.service.WebAuthnService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/webauthn")
public class WebAuthnController {

    private final WebAuthnService webAuthnService;

    public WebAuthnController(WebAuthnService webAuthnService) {
        this.webAuthnService = webAuthnService;
    }

    @PostMapping("/register/start")
    public ResponseEntity<?> startRegistration(
            @AuthenticationPrincipal UserDetails userDetails
    ) throws JsonProcessingException {
        var response = webAuthnService.startRegistration(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/finish")
    public ResponseEntity<Map<String, String>> finishRegistration(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String flowId,
            @RequestParam(required = false) String deviceName,
            @RequestBody String credentialJson
    ) {
        webAuthnService.finishRegistration(
                userDetails.getUsername(), flowId, credentialJson, deviceName
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "WebAuthn credential registered"));
    }

    @PostMapping("/login/start")
    public ResponseEntity<?> startLogin(
            @RequestParam(required = false) String username
    ) throws JsonProcessingException {
        var response = webAuthnService.startAuthentication(username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/finish")
    public ResponseEntity<AuthResponse> finishLogin(
            @RequestParam String flowId,
            @RequestBody String credentialJson,
            HttpServletRequest httpRequest
    ) {
        AuthResponse response = webAuthnService.finishAuthentication(
                flowId, credentialJson, httpRequest
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/credentials/{id}")
    public ResponseEntity<Void> removeCredential(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        webAuthnService.removeCredential(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
