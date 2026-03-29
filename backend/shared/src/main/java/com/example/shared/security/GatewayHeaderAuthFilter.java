package com.example.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class GatewayHeaderAuthFilter extends OncePerRequestFilter {

    private final GatewaySecurityProperties properties;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestSecret = request.getHeader(properties.getHeaderName());

        // 1. Проверяем секрет Gateway
        if (requestSecret == null || !constantTimeEquals(requestSecret, properties.getToken())) {
            log.warn("Unauthorized access attempt to: {}", request.getRequestURI());
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Direct access denied");
            return;
        }

        // 2. Анонимный запрос от Gateway (открытые эндпоинты)
        String anonymousHeader = request.getHeader("X-Anonymous-Request");
        if ("true".equals(anonymousHeader)) {
            AnonymousAuthenticationToken anonymousToken = new AnonymousAuthenticationToken(
                    "gateway-anonymous",
                    "anonymousUser",
                    List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
            );
            anonymousToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(anonymousToken);

            log.debug("Anonymous request from Gateway to: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Аутентифицированный запрос — парсим user хедеры
        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");
        String username = request.getHeader("X-User-Sub");

        if (username != null && userId != null && userRole != null) {
            try {
                GatewayPrincipal principal = new GatewayPrincipal(
                        Long.parseLong(userId),
                        username,
                        userRole
                );

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Authenticated request for user: {}", username);

            } catch (NumberFormatException _) {
                log.error("Malformed X-User-Id header: {}", userId);
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid User ID format");
                return;
            }
        } else {
            log.warn("Gateway secret valid but no user headers for: {}",
                    request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8)
        );
    }

    private void sendErrorResponse(HttpServletResponse response,
                                   int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}