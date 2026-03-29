package com.example.identityservice.security;

import com.example.identityservice.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    // ограничение запросов на конкретное время (запросы, время)
    private static final Map<String, int[]> RATE_LIMITS = Map.of(
            "/api/auth/login",           new int[]{10, 60},
            "/api/auth/register",        new int[]{5,  60},
            "/api/auth/password/forgot",  new int[]{3,  60},
            "/api/auth/password/reset",   new int[]{5,  60},
            "/api/auth/verify/send",      new int[]{3,  60}
    );

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        int[] limits = findLimits(path);

        if (limits != null && "POST".equalsIgnoreCase(request.getMethod())) {
            String clientIp = getClientIp(request);
            int maxAttempts = limits[0];
            Duration window = Duration.ofSeconds(limits[1]);

            if (rateLimitService.isRateLimited(clientIp, path, maxAttempts, window)) {
                long retryAfter = rateLimitService.getRetryAfterSeconds(clientIp, path);

                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setHeader("Retry-After", String.valueOf(retryAfter));
                response.getWriter().write(
                        "{\"status\":429," +
                                "\"error\":\"TOO_MANY_REQUESTS\"," +
                                "\"message\":\"Too many requests. Try again in " + retryAfter + " seconds\"}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private int[] findLimits(String path) {
        return RATE_LIMITS.get(path);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String[] parts = xForwardedFor.split(",");
            if (parts.length > 0) {
                String ip = parts[0].trim();
                if (!ip.isBlank()) {
                    return ip;
                }
            }
        }
        return request.getRemoteAddr();
    }
}
