package com.example.identityservice.controller;

import com.example.identityservice.dto.response.OAuth2AccountResponse;
import com.example.identityservice.service.OAuth2AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/oauth2")
@Tag(name = "OAuth2 аккаунты", description = "Привязанные Google/GitHub аккаунты")
public class OAuth2AccountController {

    private final OAuth2AccountService oAuth2AccountService;

    public OAuth2AccountController(OAuth2AccountService oAuth2AccountService) {
        this.oAuth2AccountService = oAuth2AccountService;
    }

    @Operation(summary = "Список привязанных OAuth2 аккаунтов")
    @GetMapping
    public ResponseEntity<List<OAuth2AccountResponse>> getLinkedAccounts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<OAuth2AccountResponse> accounts = oAuth2AccountService.getLinkedAccounts(
                userDetails.getUsername()
        );
        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Отвязать OAuth2 аккаунт")
    @DeleteMapping("/{provider}")
    public ResponseEntity<Void> unlinkAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Провайдер", example = "google")
            @PathVariable String provider
    ) {
        oAuth2AccountService.unlinkAccount(userDetails.getUsername(), provider);
        return ResponseEntity.noContent().build();
    }
}