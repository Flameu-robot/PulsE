package com.example.identityservice.controller;

import com.example.identityservice.dto.response.AuthResponse;
import com.example.identityservice.service.WebAuthnService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/webauthn")
@Tag(name = "WebAuthn", description = "Вход по отпечатку/Face ID/ключу безопасности")
public class WebAuthnController {

    private final WebAuthnService webAuthnService;

    public WebAuthnController(WebAuthnService webAuthnService) {
        this.webAuthnService = webAuthnService;
    }

    @Operation(summary = "Начать регистрацию ключа (шаг 1)")
    @PostMapping("/register/start")
    public ResponseEntity<?> startRegistration(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) throws JsonProcessingException {
        var response = webAuthnService.startRegistration(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Завершить регистрацию ключа (шаг 2)")
    @PostMapping("/register/finish")
    public ResponseEntity<Map<String, String>> finishRegistration(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID потока регистрации", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
            @RequestParam String flowId,
            @Parameter(description = "Название устройства", example = "MacBook Pro")
            @RequestParam(required = false) String deviceName,
            @RequestBody String credentialJson
    ) {
        webAuthnService.finishRegistration(
                userDetails.getUsername(), flowId, credentialJson, deviceName
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "WebAuthn credential registered"));
    }

    @Operation(summary = "Начать вход по ключу (шаг 1, без авторизации)")
    @PostMapping("/login/start")
    public ResponseEntity<?> startLogin(
            @Parameter(description = "Username (опционально)", example = "john_doe")
            @RequestParam(required = false) String username
    ) throws JsonProcessingException {
        var response = webAuthnService.startAuthentication(username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Завершить вход по ключу (шаг 2, без авторизации)")
    @PostMapping("/login/finish")
    public ResponseEntity<AuthResponse> finishLogin(
            @Parameter(description = "ID потока входа", example = "x1y2z3w4-a5b6-7890-cdef-123456789abc")
            @RequestParam String flowId,
            @RequestBody String credentialJson,
            @Parameter(hidden = true) HttpServletRequest httpRequest
    ) {
        AuthResponse response = webAuthnService.finishAuthentication(
                flowId, credentialJson, httpRequest
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Удалить зарегистрированный ключ")
    @DeleteMapping("/credentials/{id}")
    public ResponseEntity<Void> removeCredential(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID ключа", example = "1")
            @PathVariable Long id
    ) {
        webAuthnService.removeCredential(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}