package com.example.identityservice.controller;

import com.example.identityservice.dto.response.OAuth2AccountResponse;
import com.example.identityservice.service.OAuth2AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/oauth2")
public class OAuth2AccountController {

    private final OAuth2AccountService oAuth2AccountService;

    public OAuth2AccountController(OAuth2AccountService oAuth2AccountService) {
        this.oAuth2AccountService = oAuth2AccountService;
    }

    @GetMapping
    public ResponseEntity<List<OAuth2AccountResponse>> getLinkedAccounts(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<OAuth2AccountResponse> accounts = oAuth2AccountService.getLinkedAccounts(
                userDetails.getUsername()
        );
        return ResponseEntity.ok(accounts);
    }

    @DeleteMapping("/{provider}")
    public ResponseEntity<Void> unlinkAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String provider
    ) {
        oAuth2AccountService.unlinkAccount(userDetails.getUsername(), provider);
        return ResponseEntity.noContent().build();
    }
}