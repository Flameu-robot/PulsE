package com.example.feedservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class GatewayHeaderFilter extends OncePerRequestFilter {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException
    {


        String userIdHeader = request.getHeader(USER_ID_HEADER);
        String roleHeader = request.getHeader(USER_ROLE_HEADER);

        if (StringUtils.hasText(userIdHeader)) {
            try {
                Long userId = Long.parseLong(userIdHeader);

                var authorities = StringUtils.hasText(roleHeader)
                        ? List.of(new SimpleGrantedAuthority("ROLE_" + roleHeader.toUpperCase()))
                        : List.<SimpleGrantedAuthority>of();

                var authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("User authenticated via gateway: id={}, role={}", userId, roleHeader);

            } catch (NumberFormatException e) {
                log.warn("Invalid X-User-Id header: {}", userIdHeader);
            }
        }

        filterChain.doFilter(request, response);
    }
}
