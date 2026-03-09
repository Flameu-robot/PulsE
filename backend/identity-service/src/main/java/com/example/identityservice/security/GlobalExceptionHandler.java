package com.example.identityservice.security;

import exception.auth.EmailNotVerifiedException;
import exception.base.BaseException;
import exception.base.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException ex, HttpServletRequest request
    ) {
        log.warn("Business exception: {} at {}", ex.getMessage(), request.getRequestURI());

        ErrorResponse response = new ErrorResponse(
                ex.getStatusCode(),
                ex.getErrorType().name(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonError(
            org.springframework.http.converter.HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.error("JSON parsing error at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse response = new ErrorResponse(
                400,
                "BAD_REQUEST",
                "Malformed JSON request: " + ex.getMostSpecificCause().getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request
    ) {
        Map<String, String> details = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Validation failed for {}: {}", request.getRequestURI(), details);

        ErrorResponse response = new ErrorResponse(
                422,
                "VALIDATION",
                "Validation failed",
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(422).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request
    ) {
        log.warn("Bad credentials attempt at {}", request.getRequestURI());
        ErrorResponse response = new ErrorResponse(
                401,
                "AUTHENTICATION",
                "Invalid username or password",
                request.getRequestURI()
        );
        return ResponseEntity.status(401).body(response);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UsernameNotFoundException ex, HttpServletRequest request
    ) {
        log.warn("User not found: {} at {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse response = new ErrorResponse(
                404,
                "NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request
    ) {
        log.error("INTERNAL SERVER ERROR at {}: ", request.getRequestURI(), ex);

        ErrorResponse response = new ErrorResponse(
                500,
                "INTERNAL",
                "Internal server error: " + ex.getClass().getSimpleName(), // Добавим имя класса ошибки для удобства
                request.getRequestURI()
        );
        return ResponseEntity.status(500).body(response);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotVerified(
            EmailNotVerifiedException ex,
            HttpServletRequest request
    ) {
        log.warn("Email not verified: at {}", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErrorResponse(
                        403,
                        "EMAIL_NOT_VERIFIED",
                        ex.getMessage(),
                        request.getRequestURI()
                )
        );
    }
}