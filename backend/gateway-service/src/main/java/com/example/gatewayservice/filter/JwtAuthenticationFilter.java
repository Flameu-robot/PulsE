package com.example.gatewayservice.filter;

import com.example.gatewayservice.dto.RouteRule;
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
    private final List<RouteRule> openApiEndpoints = List.of(
            // Identity-service
            new RouteRule("POST", "/api/auth/register"),
            new RouteRule("POST", "/api/auth/login"),
            new RouteRule("POST", "/api/auth/refresh"),
            new RouteRule("POST", "/api/auth/password/forgot"),
            new RouteRule("POST", "/api/auth/password/reset"),
            new RouteRule("POST", "/api/auth/webauthn/login"),
            new RouteRule("GET", "/api/users/{id:\\d+}"),

            new RouteRule(null, "/api/auth/oauth2/**"),
            new RouteRule(null, "/oauth2/**"),

            // Swagger
            new RouteRule(null, "/swagger-ui.html"),
            new RouteRule(null, "/swagger-ui/**"),
            new RouteRule(null, "/v3/api-docs/**"),
            new RouteRule(null, "/v3/api-docs"),
            new RouteRule(null, "/webjars/**")
    );

    @SuppressWarnings("NullableProblems")
    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();

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

        boolean isOpen = isOpenEndpoint(path, method);

        // 2. Извлечение хедера Authorization
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // 3. Если токен передан (даже для открытых эндпоинтов) - пытаемся его распарсить
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (!jwtUtils.validateToken(token)) {
                // Если токен невалидный (просрочен/подделан), мы отклоняем запрос
                return onError(exchange, "Invalid JWT Token");
            }

            // Токен валиден, извлекаем данные
            Claims claims = jwtUtils.getAllClaimsFromToken(token);
            Object userId = claims.get("userId");
            Object role = claims.get("role");
            String subject = claims.getSubject();

            if (userId == null || role == null || subject == null) {
                return onError(exchange, "JWT is missing required claims");
            }

            // Подставляем данные пользователя в хедеры
            requestBuilder.header("X-User-Id", String.valueOf(userId));
            requestBuilder.header("X-User-Role", String.valueOf(role));
            requestBuilder.header("X-User-Sub", subject);

        } else {
            // 4. Токена нет
            if (isOpen) {
                // Если путь открытый, помечаем запрос как анонимный
                requestBuilder.header("X-Anonymous-Request", "true");
            } else {
                // Если путь закрытый и токена нет — ошибка авторизации
                return onError(exchange, "Missing or invalid Authorization header");
            }
        }

        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }

    private boolean isOpenEndpoint(String path, String method) {
        if (isPublicUserProfile(path, method)) {
            return true;
        }

        return openApiEndpoints.stream()
                .anyMatch(rule ->
                        pathMatcher.match(rule.pattern(), path)
                                && (rule.method() == null || rule.method().equalsIgnoreCase(method))
                );
    }

    private boolean isPublicUserProfile(String path, String method) {
        return "GET".equalsIgnoreCase(method)
                && path.matches("^/api/users/\\d+$");
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