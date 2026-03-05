package com.example.gatewayservice.filter;

import com.example.gatewayservice.security.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;

    // Список путей, не требующих авторизации
    private final List<String> openApiEndpoints = List.of(
            "/auth/login",
            "/auth/register",
            "/v3/api-docs"
    );

    @SuppressWarnings("NullableProblems")
    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Пропуск открытых эндпоинтов
        if (openApiEndpoints.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // Получение заголовка
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || authHeader.isEmpty()) {
            return onError(exchange, "Authorization header is missing");
        }

        // Формат Bearer
        if (!authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Invalid Authorization header format");
        }

        // Удаление Bearer
        String token = authHeader.substring(7);

        // Валидация токена
        if (!jwtUtils.validateToken(token)) {
            return onError(exchange, "Invalid JWT Token");
        }

        // Парсинг клеймов в хедеры
        Claims claims = jwtUtils.getAllClaimsFromToken(token);

        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", String.valueOf(claims.get("userId")))
                .header("X-User-Role", String.valueOf(claims.get("role")))
                .header("X-User-Sub", claims.getSubject())
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    @SuppressWarnings("NullableProblems")
    private Mono<Void> onError(ServerWebExchange exchange, String err) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        log.error("Security Error: {}", err);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}