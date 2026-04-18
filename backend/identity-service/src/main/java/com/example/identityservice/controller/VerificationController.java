package com.example.identityservice.controller;

import com.example.identityservice.dto.request.ForgotPasswordRequest;
import com.example.identityservice.dto.request.ResetPasswordRequest;
import com.example.identityservice.dto.request.VerifyEmailRequest;
import com.example.identityservice.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Верификация", description = "Подтверждение email, сброс пароля")
public class VerificationController {

    private final VerificationService verificationService;
    private final PasswordEncoder passwordEncoder;

    public VerificationController(
            VerificationService verificationService,
            PasswordEncoder passwordEncoder
    ) {
        this.verificationService = verificationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "Отправить код подтверждения на email")
    @PostMapping("/verify/send")
    public ResponseEntity<Map<String, String>> sendVerificationCode(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        verificationService.sendVerificationCode(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Verification code sent"));
    }

    @Operation(summary = "Подтвердить email кодом")
    @PostMapping("/verify/confirm")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        verificationService.verifyEmail(userDetails.getUsername(), request.code());
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    @Operation(summary = "Запросить сброс пароля (без авторизации)")
    @PostMapping("/password/forgot")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        verificationService.sendPasswordResetCode(request.email());
        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset code has been sent"));
    }

    @Operation(summary = "Сбросить пароль по коду (без авторизации)")
    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        String hashedPassword = passwordEncoder.encode(request.newPassword());
        verificationService.resetPassword(request.email(), request.code(), hashedPassword);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}