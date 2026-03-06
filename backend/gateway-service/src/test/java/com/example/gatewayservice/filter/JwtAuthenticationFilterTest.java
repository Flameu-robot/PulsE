package com.example.gatewayservice.filter;

import com.example.gatewayservice.security.JwtUtils;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Jwt Auth Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        // По умолчанию chain.filter должен возвращать пустой Mono (success),
        // иначе тесты будут падать с NullPointerException
        lenient().when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Skip open endpoints")
    void shouldPassOpenEndpointsWithoutToken() {

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v3/api-docs/else").build()
        );

        // Запуск фильтра
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        // chain.filter был вызван
        verify(chain).filter(any(ServerWebExchange.class));
        // jwtUtils не вызывался
        verifyNoInteractions(jwtUtils);
    }

    @Test
    @DisplayName("401 when Authorization header is missing")
    void shouldReturn401WhenNoHeader() {

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users").build()
        );

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("401 when Authorization header is empty")
    void shouldReturn401WhenHeaderEmpty() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .build()
        );

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("401 when Authorization header has wrong format")
    void shouldReturn401WhenNotBearerFormat() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Basic 12345")
                        .build()
        );

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("401 when token is invalid")
    void shouldReturn401WhenTokenInvalid() {
        String token = "invalid.token.value";
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build()
        );

        when(jwtUtils.validateToken(token)).thenReturn(false);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("Valid token: headers are added to downstream request")
    void shouldAddHeadersWhenTokenValid() {
        String token = "valid.token.value";
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build()
        );

        when(jwtUtils.validateToken(token)).thenReturn(true);

        Claims claims = mock(Claims.class);
        when(claims.get("userId")).thenReturn(3);
        when(claims.get("role")).thenReturn("USER");
        when(claims.getSubject()).thenReturn("Ziragon");
        when(jwtUtils.getAllClaimsFromToken(token)).thenReturn(claims);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        HttpHeaders headers = captor.getValue().getRequest().getHeaders();
        assertThat(headers.getFirst("X-User-Id")).isEqualTo("3");
        assertThat(headers.getFirst("X-User-Role")).isEqualTo("USER");
        assertThat(headers.getFirst("X-User-Sub")).isEqualTo("Ziragon");
    }

    // Spoofing - подмена заголовков в запросе
    @Test
    @DisplayName("Spoofed X-User headers are stripped on protected endpoint")
    void shouldStripSpoofedHeadersOnProtectedEndpoint() {
        String token = "valid.token.value";
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header("X-User-Id", "999")
                        .header("X-User-Role", "ADMIN")
                        .header("X-User-Sub", "hacker")
                        .build()
        );

        when(jwtUtils.validateToken(token)).thenReturn(true);

        Claims claims = mock(Claims.class);
        when(claims.get("userId")).thenReturn(3);
        when(claims.get("role")).thenReturn("USER");
        when(claims.getSubject()).thenReturn("Ziragon");
        when(jwtUtils.getAllClaimsFromToken(token)).thenReturn(claims);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        HttpHeaders headers = captor.getValue().getRequest().getHeaders();
        assertThat(headers.getFirst("X-User-Id")).isEqualTo("3");
        assertThat(headers.getFirst("X-User-Role")).isEqualTo("USER");
        assertThat(headers.getFirst("X-User-Sub")).isEqualTo("Ziragon");
    }
}