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
    @DisplayName("Skip open endpoints (login/register)")
    void shouldPassOpenEndpointsWithoutToken() {

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/auth/login").build()
        );

        // Запуск фильтра
        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        // chain.filter был вызван
        verify(chain).filter(exchange);
        // jwtUtils не вызывался
        verifyNoInteractions(jwtUtils);
    }

    @Test
    @DisplayName("401 without header Authorization")
    void shouldReturn401WhenNoHeader() {

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users").build()
        );

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("401 without Bearer format")
    void shouldReturn401WhenInvalidHeaderFormat() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Basic 12345")
                        .build()
        );

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("401 invalid token")
    void shouldReturn401WhenTokenInvalid() {
        String token = "invalid.token.value";
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build()
        );

        when(jwtUtils.validateToken(token)).thenReturn(false);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("Success: HttpRequest received the necessary headers")
    void shouldAddHeadersWhenTokenIsValid() {
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

        filter.filter(exchange, chain).block();

        // Перехват запроса
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        ServerWebExchange processedExchange = captor.getValue();
        HttpHeaders headers = processedExchange.getRequest().getHeaders();

        assertThat(headers.getFirst("X-User-Id")).isEqualTo("3");
        assertThat(headers.getFirst("X-User-Role")).isEqualTo("USER");
        assertThat(headers.getFirst("X-User-Sub")).isEqualTo("Ziragon");
    }
}