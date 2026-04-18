package com.example.identityservice.controller;

import com.example.identityservice.dto.request.UpdateProfileRequest;
import com.example.identityservice.dto.response.PublicUserResponse;
import com.example.identityservice.dto.response.UserResponse;
import com.example.identityservice.service.UserService;
import com.example.shared.security.GatewayPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Пользователи", description = "Профиль и управление аккаунтом")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Мой профиль (полный)")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal GatewayPrincipal principal
    ) {
        return ResponseEntity.ok(userService.getProfile(principal.getUsername()));
    }

    @Operation(summary = "Обновить мой профиль (displayName, bio, phone, avatarUrl)")
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal GatewayPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateProfile(principal.getUsername(), request));
    }

    @Operation(summary = "Удалить мой аккаунт")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal GatewayPrincipal principal
    ) {
        userService.deleteAccount(principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Публичный профиль по ID")
    @GetMapping("/{id}")
    public ResponseEntity<PublicUserResponse> getPublicProfile(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(userService.getPublicProfile(id));
    }

    @Operation(summary = "Публичный профиль по @username (поиск по тегу)")
    @GetMapping("/by-username/{username}")
    public ResponseEntity<PublicUserResponse> getPublicProfileByUsername(
            @Parameter(description = "Username пользователя (тег)", example = "john_doe")
            @PathVariable String username
    ) {
        return ResponseEntity.ok(userService.getPublicProfileByUsername(username));
    }
}
