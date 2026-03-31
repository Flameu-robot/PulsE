package com.example.identityservice.controller;

import com.example.identityservice.dto.request.UpdateProfileRequest;
import com.example.identityservice.dto.response.PublicUserResponse;
import com.example.identityservice.dto.response.UserResponse;
import com.example.identityservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserResponse response = userService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Обновить мой профиль")
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserResponse response = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Удалить мой аккаунт")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        userService.deleteAccount(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Публичный профиль пользователя (без авторизации)")
    @GetMapping("/{id}")
    public ResponseEntity<PublicUserResponse> getPublicProfile(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long id
    ) {
        PublicUserResponse response = userService.getPublicProfile(id);
        return ResponseEntity.ok(response);
    }
}