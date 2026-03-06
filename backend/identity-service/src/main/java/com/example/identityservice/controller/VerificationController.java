package com.example.identityservice.controller;

import com.example.identityservice.dto.request.ForgotPasswordRequest;
import com.example.identityservice.dto.request.ResetPasswordRequest;
import com.example.identityservice.dto.request.VerifyEmailRequest;
import com.example.identityservice.service.VerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
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

    @PostMapping("/verify/send")
    public ResponseEntity<Map<String, String>> sendVerificationCode(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        verificationService.sendVerificationCode(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Verification code sent"));
    }

    @PostMapping("/verify/confirm")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        verificationService.verifyEmail(userDetails.getUsername(), request.code());
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        verificationService.sendPasswordResetCode(request.email());
        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset code has been sent"));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        String hashedPassword = passwordEncoder.encode(request.newPassword());
        verificationService.resetPassword(request.email(), request.code(), hashedPassword);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}