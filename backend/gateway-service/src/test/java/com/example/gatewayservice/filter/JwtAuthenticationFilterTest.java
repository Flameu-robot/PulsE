package com.example.gatewayservice.filter;

import com.example.gatewayservice.security.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private static final String SECRET_HEADER = "X-Internal-Secret";
    private static final String SECRET_TOKEN = "test-secret-token";
    private static final String PROTECTED_PATH = "/api/music";

    @BeforeEach
    void setUp() {
        // Set @Value
        ReflectionTestUtils.setField(filter, "secretHeaderName", SECRET_HEADER);
        ReflectionTestUtils.setField(filter, "secretToken", SECRET_TOKEN);

        lenient().when(chain.filter(any(ServerWebExchange.class)))
                .thenReturn(Mono.empty());
    }

    // -- Help-методы --
    // Захват хедеров
    private HttpHeaders captureDownstreamHeaders() {
        ArgumentCaptor<ServerWebExchange> captor =
                ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        return captor.getValue().getRequest().getHeaders();
    }

    // Мок клеймов токена
    private Claims createClaims(Object userId, Object role, String subject) {
        var builder = Jwts.claims();

        if (subject != null) {
            builder.subject(subject);
        }

        if (userId != null) {
            builder.add("userId", userId);
        }
        if (role != null) {
            builder.add("role", role);
        }

        return builder.build();
    }

    // Имитация http запроса к protected руту
    private MockServerWebExchange protectedExchange(String token) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.get(PROTECTED_PATH)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build()
        );
    }

    // -- Тесты --
    @Nested
    @DisplayName("Open endpoints")
    class OpenEndpoints {

        @Test
        @DisplayName("Passes through without token")
        void shouldPassWithoutToken() {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.post("/v3/api-docs/else").build()
            );

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            verify(chain).filter(any(ServerWebExchange.class));
            verifyNoInteractions(jwtUtils);
        }

        @Test
        @DisplayName("Adds X-Anonymous-Request header")
        void shouldAddAnonymousHeader() {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/auth/login").build()
            );

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            HttpHeaders headers = captureDownstreamHeaders();
            assertThat(headers.getFirst("X-Anonymous-Request")).isEqualTo("true");
        }

        @Test
        @DisplayName("Adds internal secret header")
        void shouldAddInternalSecretHeader() {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/users/123").build()
            );

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            HttpHeaders headers = captureDownstreamHeaders();
            assertThat(headers.getFirst(SECRET_HEADER)).isEqualTo(SECRET_TOKEN);
        }

        @Test
        @DisplayName("Strips spoofed headers on open endpoint")
        void shouldStripSpoofedHeadersOnOpenEndpoint() {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/auth/register")
                            .header("X-User-Id", "999")
                            .header("X-User-Role", "ADMIN")
                            .header("X-User-Sub", "hacker")
                            .header("X-Anonymous-Request", "false")
                            .header(SECRET_HEADER, "fake-secret")
                            .build()
            );

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            HttpHeaders headers = captureDownstreamHeaders();
            assertThat(headers.getFirst("X-User-Id")).isNull();
            assertThat(headers.getFirst("X-User-Role")).isNull();
            assertThat(headers.getFirst("X-User-Sub")).isNull();
            assertThat(headers.getFirst("X-Anonymous-Request")).isEqualTo("true");
            assertThat(headers.getFirst(SECRET_HEADER)).isEqualTo(SECRET_TOKEN);
        }
    }

    @Nested
    @DisplayName("Missing / invalid Authorization")
    class MissingAuth {

        @Test
        @DisplayName("401 when header is missing")
        void shouldReturn401WhenNoHeader() {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get(PROTECTED_PATH).build()
            );

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            assertThat(exchange.getResponse().getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any());
        }

        @Test
        @DisplayName("401 when header is empty")
        void shouldReturn401WhenHeaderEmpty() {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get(PROTECTED_PATH)
                            .header(HttpHeaders.AUTHORIZATION, "")
                            .build()
            );

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            assertThat(exchange.getResponse().getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any());
        }

        @Test
        @DisplayName("401 when not Bearer format")
        void shouldReturn401WhenNotBearerFormat() {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get(PROTECTED_PATH)
                            .header(HttpHeaders.AUTHORIZATION, "Basic 12345")
                            .build()
            );

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            assertThat(exchange.getResponse().getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any());
        }

        @Test
        @DisplayName("401 when token is invalid")
        void shouldReturn401WhenTokenInvalid() {
            String token = "invalid.token.value";
            MockServerWebExchange exchange = protectedExchange(token);

            when(jwtUtils.validateToken(token)).thenReturn(false);

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            assertThat(exchange.getResponse().getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any());
        }
    }

    @Nested
    @DisplayName("Valid token")
    class ValidToken {

        private static final String TOKEN = "valid.token.value";

        @Test
        @DisplayName("Adds user headers from JWT claims")
        void shouldAddUserHeaders() {
            MockServerWebExchange exchange = protectedExchange(TOKEN);
            when(jwtUtils.validateToken(TOKEN)).thenReturn(true);
            when(jwtUtils.getAllClaimsFromToken(TOKEN))
                    .thenReturn(createClaims(3, "USER", "Ziragon"));

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            HttpHeaders headers = captureDownstreamHeaders();
            assertThat(headers.getFirst("X-User-Id")).isEqualTo("3");
            assertThat(headers.getFirst("X-User-Role")).isEqualTo("USER");
            assertThat(headers.getFirst("X-User-Sub")).isEqualTo("Ziragon");
        }

        @Test
        @DisplayName("Adds internal secret header")
        void shouldAddInternalSecretOnProtectedEndpoint() {
            MockServerWebExchange exchange = protectedExchange(TOKEN);
            when(jwtUtils.validateToken(TOKEN)).thenReturn(true);
            when(jwtUtils.getAllClaimsFromToken(TOKEN))
                    .thenReturn(createClaims(1, "USER", "user"));

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            HttpHeaders headers = captureDownstreamHeaders();
            assertThat(headers.getFirst(SECRET_HEADER)).isEqualTo(SECRET_TOKEN);
        }

        @Test
        @DisplayName("Authorization header is NOT forwarded downstream")
        void shouldRemoveAuthorizationHeader() {
            MockServerWebExchange exchange = protectedExchange(TOKEN);
            when(jwtUtils.validateToken(TOKEN)).thenReturn(true);
            when(jwtUtils.getAllClaimsFromToken(TOKEN))
                    .thenReturn(createClaims(1, "USER", "user"));

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            HttpHeaders headers = captureDownstreamHeaders();
            assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isNull();
        }

        @Test
        @DisplayName("Spoofed headers are replaced with JWT values")
        void shouldReplaceSpoofedHeaders() {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get(PROTECTED_PATH)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                            .header("X-User-Id", "999")
                            .header("X-User-Role", "ADMIN")
                            .header("X-User-Sub", "hacker")
                            .header(SECRET_HEADER, "fake-secret")
                            .header("X-Anonymous-Request", "true")
                            .build()
            );

            when(jwtUtils.validateToken(TOKEN)).thenReturn(true);
            when(jwtUtils.getAllClaimsFromToken(TOKEN))
                    .thenReturn(createClaims(3, "USER", "Ziragon"));

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            HttpHeaders headers = captureDownstreamHeaders();
            assertThat(headers.getFirst("X-User-Id")).isEqualTo("3");
            assertThat(headers.getFirst("X-User-Role")).isEqualTo("USER");
            assertThat(headers.getFirst("X-User-Sub")).isEqualTo("Ziragon");
            assertThat(headers.getFirst(SECRET_HEADER)).isEqualTo(SECRET_TOKEN);
            assertThat(headers.getFirst("X-Anonymous-Request")).isNull();
        }

        @Test
        @DisplayName("X-Anonymous-Request is NOT set on protected endpoint")
        void shouldNotSetAnonymousOnProtectedEndpoint() {
            MockServerWebExchange exchange = protectedExchange(TOKEN);
            when(jwtUtils.validateToken(TOKEN)).thenReturn(true);
            when(jwtUtils.getAllClaimsFromToken(TOKEN))
                    .thenReturn(createClaims(1, "USER", "user"));

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            HttpHeaders headers = captureDownstreamHeaders();
            assertThat(headers.getFirst("X-Anonymous-Request")).isNull();
        }
    }

    @Nested
    @DisplayName("Missing JWT claims")
    class MissingClaims {

        private static final String TOKEN = "valid.token.value";

        @Test
        @DisplayName("401 when userId claim is null")
        void shouldReturn401WhenUserIdNull() {
            MockServerWebExchange exchange = protectedExchange(TOKEN);
            when(jwtUtils.validateToken(TOKEN)).thenReturn(true);
            when(jwtUtils.getAllClaimsFromToken(TOKEN))
                    .thenReturn(createClaims(null, "USER", "Ziragon"));

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            assertThat(exchange.getResponse().getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any());
        }

        @Test
        @DisplayName("401 when role claim is null")
        void shouldReturn401WhenRoleNull() {
            MockServerWebExchange exchange = protectedExchange(TOKEN);
            when(jwtUtils.validateToken(TOKEN)).thenReturn(true);
            when(jwtUtils.getAllClaimsFromToken(TOKEN))
                    .thenReturn(createClaims(1, null, "Ziragon"));

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            assertThat(exchange.getResponse().getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any());
        }

        @Test
        @DisplayName("401 when subject is null")
        void shouldReturn401WhenSubjectNull() {
            MockServerWebExchange exchange = protectedExchange(TOKEN);
            when(jwtUtils.validateToken(TOKEN)).thenReturn(true);
            when(jwtUtils.getAllClaimsFromToken(TOKEN))
                    .thenReturn(createClaims(1, "USER", null));

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            assertThat(exchange.getResponse().getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any());
        }
    }
}