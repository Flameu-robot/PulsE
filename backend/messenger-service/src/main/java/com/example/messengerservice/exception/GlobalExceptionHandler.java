package com.example.messengerservice.exception;

import exception.base.BaseException;
import exception.base.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonError(
            org.springframework.http.converter.HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.error("JSON parsing error at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                400,
                "BAD_REQUEST",
                "Malformed JSON request",
                request.getRequestURI()
        );
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request
    ) {
        log.error("INTERNAL SERVER ERROR at {}: ", request.getRequestURI(), ex);

        ErrorResponse response = new ErrorResponse(
                500,
                "INTERNAL",
                "Internal server error: " + ex.getClass().getSimpleName(),
                request.getRequestURI()
        );
        return ResponseEntity.status(500).body(response);
    }
}