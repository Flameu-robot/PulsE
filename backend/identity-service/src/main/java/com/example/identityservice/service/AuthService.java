package com.example.identityservice.service;

import com.example.identityservice.dto.request.ChangePasswordRequest;
import com.example.identityservice.dto.request.LoginRequest;
import com.example.identityservice.dto.request.RefreshTokenRequest;
import com.example.identityservice.dto.request.RegisterRequest;
import com.example.identityservice.dto.response.AuthResponse;
import com.example.identityservice.dto.response.UserResponse;
import com.example.identityservice.entity.RefreshToken;
import com.example.identityservice.entity.User;
import com.example.identityservice.entity.enums.UserRole;
import com.example.identityservice.entity.enums.UserStatus;
import com.example.identityservice.repository.TokenRepository;
import com.example.identityservice.repository.UserRepository;
import exception.auth.InvalidCredentialsException;
import exception.auth.TokenException;
import exception.auth.UserAlreadyExistsException;
import exception.auth.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuthResponseFactory authResponseFactory;

    public AuthService(
            UserRepository userRepository,
            TokenRepository refreshTokenRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            AuthResponseFactory authResponseFactory
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.authResponseFactory = authResponseFactory;
    }

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username", request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email", request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .status(UserStatus.PENDING)
                .role(UserRole.USER)
                .build();

        user = userRepository.save(user);

        return authResponseFactory.create(user, httpRequest);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String login = request.login();
        User user;

        if (login.contains("@")) {
            user = userRepository.findByEmail(login)
                    .orElseThrow(InvalidCredentialsException::new);
        } else {
            user = userRepository.findByUsername(login)
                    .orElseThrow(InvalidCredentialsException::new);
        }

        if (user.isLocked()) {
            throw new TokenException("Account is locked until " + user.getLockedUntil() +
                    ". Try again later.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), request.password())
            );
        } catch (AuthenticationException e) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setLockedUntil(OffsetDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                userRepository.save(user);
                throw new TokenException("Account locked for " + LOCK_DURATION_MINUTES +
                        " minutes after " + MAX_FAILED_ATTEMPTS + " failed attempts");
            }

            userRepository.save(user);
            throw new InvalidCredentialsException();
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        return authResponseFactory.create(user, httpRequest);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String rawToken = request.refreshToken();

        if (!jwtService.isTokenValid(rawToken)) {
            throw new TokenException("Invalid refresh token");
        }

        if (!"refresh".equals(jwtService.extractTokenType(rawToken))) {
            throw new TokenException("Token is not a refresh token");
        }

        String tokenHash = authResponseFactory.hashToken(rawToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new TokenException("Refresh token not found"));

        if (!storedToken.isActive()) {
            throw new TokenException("Refresh token is revoked or expired");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();

        return authResponseFactory.create(user, httpRequest);
    }

    @Transactional
    public void logout(String username, String refreshToken) {
        String tokenHash = authResponseFactory.hashToken(refreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new TokenException("Token not found"));

        if (!storedToken.getUser().getUsername().equals(username)) {
            throw new TokenException("Token does not belong to user");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
    }

    @Transactional
    public void logoutAll(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}
