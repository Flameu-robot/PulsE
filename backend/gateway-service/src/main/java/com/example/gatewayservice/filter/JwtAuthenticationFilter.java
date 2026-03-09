package com.example.gatewayservice.filter;

import com.example.gatewayservice.security.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${internal.security.header-name:X-Internal-Secret}")
    private String secretHeaderName;

    @Value("${internal.security.token}")
    private String secretToken;

    private final JwtUtils jwtUtils;

    private final AntPathMatcher pathMatcher =
            new AntPathMatcher();

    // Список путей, не требующих авторизации
    private final List<String> openApiEndpoints = List.of(
            // Identity-service
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/password/forgot",
            "/api/auth/password/reset",
            "/api/auth/webauthn/login",

            "/api/users/**",
            "/api/auth/oauth2/**",
            "/oauth2/**",

            // Swagger
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/webjars/**"
    );

    @SuppressWarnings("NullableProblems")
    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 1. Очистка входящих хедеров (безопасность)
        ServerHttpRequest.Builder requestBuilder = request.mutate()
                .headers(h -> {
                    h.remove("X-User-Id");
                    h.remove("X-User-Role");
                    h.remove("X-User-Sub");
                    h.remove("X-Anonymous-Request");
                    h.remove(HttpHeaders.AUTHORIZATION);
                    h.remove(secretHeaderName);
                });

        requestBuilder.header(secretHeaderName, secretToken);

        // Пропуск открытых эндпоинтов
        if (isOpenEndpoint(path)) {
            // Маркер отсутствия аутентификации
            requestBuilder.header("X-Anonymous-Request", "true");
            return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
        }

        // Извлечение хедеров из запроса
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return onError(exchange, "Invalid JWT Token");
        }

        // Подстановка в хедеры
        Claims claims = jwtUtils.getAllClaimsFromToken(token);
        Object userId = claims.get("userId");
        Object role = claims.get("role");
        String subject = claims.getSubject();

        if (userId == null || role == null || subject == null) {
            return onError(exchange, "JWT is missing required claims");
        }

        requestBuilder.header("X-User-Id", String.valueOf(userId));
        requestBuilder.header("X-User-Role", String.valueOf(role));
        requestBuilder.header("X-User-Sub", subject);

        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }

    private boolean isOpenEndpoint(String path) {
        return openApiEndpoints.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @SuppressWarnings("NullableProblems")
    private Mono<Void> onError(ServerWebExchange exchange, String err) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        log.error("Security Error: {}", err);

        String body = "{\"error\": \"" + err + "\"}";
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}