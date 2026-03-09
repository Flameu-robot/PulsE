package com.example.identityservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@Slf4j
public class GatewayHeaderAuthFilter extends OncePerRequestFilter {

    @Value("${internal.security.header-name:X-Internal-Secret}")
    private String secretHeaderName;

    @Value("${internal.security.token}")
    private String secretToken;

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

        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");
        String username = request.getHeader("X-User-Sub");
        String requestSecret = request.getHeader(secretHeaderName);

        if (requestSecret == null || !constantTimeEquals(requestSecret, secretToken)) {
            log.warn("Unauthorized internal access attempt to: {}", request.getRequestURI());
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN,
                    "Direct access denied");
            return;  // НЕ вызываем filterChain.doFilter
        }

        if (username != null && userId != null && userRole != null) {
            try {
                log.debug("Attempting internal auth for user: {}, id: {}", username, userId);

                GatewayPrincipal principal = new GatewayPrincipal(
                        Long.parseLong(userId),
                        username,
                        userRole
                );

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.info("Successfully authenticated internal request for user: {}", username);

            } catch (NumberFormatException e) {
                log.error("MALFORMED HEADER: X-User-Id must be Long. Value received: {}", userId);
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid User ID format");
                return;
            } catch (Exception e) {
                log.error("Unexpected error during internal authentication for path {}: {}",
                        request.getRequestURI(), e.getMessage(), e);
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Internal Auth Processing Error");
                return;
            }
        } else {
            log.trace("Missing user headers (Id/Role/Sub) for internal request to: {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    // Защита против Timing Attack
    private boolean constantTimeEquals(String requestSecret, String secretToken) {
        return MessageDigest.isEqual(
                requestSecret.getBytes(StandardCharsets.UTF_8),
                secretToken.getBytes(StandardCharsets.UTF_8));
    }

    private void sendErrorResponse(HttpServletResponse response,
                                   int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}