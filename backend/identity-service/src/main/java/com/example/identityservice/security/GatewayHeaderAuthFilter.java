package com.example.identityservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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

        if (requestSecret == null || !requestSecret.equals(secretToken)) {
            log.warn("Unauthorized internal access attempt detected!");
            filterChain.doFilter(request, response);
            return;
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
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid User ID format");
                return;
            } catch (Exception e) {
                log.error("Unexpected error during internal authentication for path {}: {}",
                        request.getRequestURI(), e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Internal Auth Processing Error\"}");
                return;
            }
        } else {
            log.trace("Missing user headers (Id/Role/Sub) for internal request to: {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}