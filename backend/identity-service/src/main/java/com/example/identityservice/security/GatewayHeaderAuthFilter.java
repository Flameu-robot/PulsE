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
@Order(1)
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

            } catch (NumberFormatException e) {
            }
        }

        filterChain.doFilter(request, response);
    }
}