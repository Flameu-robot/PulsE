package com.example.identityservice.security;

import com.example.identityservice.config.OAuth2Properties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationFailureHandler
        extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(
            OAuth2AuthenticationFailureHandler.class
    );

    private final OAuth2Properties oAuth2Properties;

    public OAuth2AuthenticationFailureHandler(OAuth2Properties oAuth2Properties) {
        this.oAuth2Properties = oAuth2Properties;
    }

    @Override
    public void onAuthenticationFailure(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AuthenticationException exception
    ) throws IOException {

        log.error("OAuth2 authentication failed: {}", exception.getMessage());

        String errorMessage = URLEncoder.encode(
                exception.getLocalizedMessage(),
                StandardCharsets.UTF_8
        );

        String targetUrl = UriComponentsBuilder
                .fromUriString(oAuth2Properties.defaultRedirectUri())
                .queryParam("error", "oauth2_error")
                .queryParam("message", errorMessage)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}