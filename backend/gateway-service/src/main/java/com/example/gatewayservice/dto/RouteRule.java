package com.example.gatewayservice.dto;

public record RouteRule(
        String method,
        String pattern
) { }