package com.example.identityservice.controller;

import com.example.identityservice.config.RedisCleanup;
import com.example.identityservice.config.RedisTestContainerConfig;
import com.example.identityservice.dto.request.RegisterRequest;
import com.example.identityservice.dto.response.AuthResponse;
import com.example.identityservice.service.AuthService;
import exception.auth.UserAlreadyExistsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "gateway.security.token=test-secret-token",
        "gateway.security.header-name=X-Internal-Secret"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest extends RedisTestContainerConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisCleanup redisCleanup;

    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Value("${internal.security.header-name}")
    private String secretHeaderName;

    @Value("${internal.security.token}")
    private String secretToken;

    private final AuthResponse successResponse = new AuthResponse(
            "access-token",
            "refresh-token",
            1L,
            "testuser",
            "USER"
    );

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        redisCleanup.flushRateLimits();
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {

        @Test
        @DisplayName("should return 201 on successful registration")
        void shouldReturn201OnSuccess() throws Exception {
            when(authService.register(any(), any())).thenReturn(successResponse);

            RegisterRequest request = new RegisterRequest("testuser", "test@test.com", "password123");

            mockMvc.perform(post("/api/auth/register")
                            .header(secretHeaderName, secretToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.userId").value(1));
        }

        @ParameterizedTest
        @MethodSource("invalidRegistrationRequests")
        @DisplayName("should return 422 on invalid input")
        void shouldReturn422OnInvalidInput(RegisterRequest request) throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .header(secretHeaderName, secretToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.error").value("VALIDATION"));
        }

        static Stream<RegisterRequest> invalidRegistrationRequests() {
            return Stream.of(
                    new RegisterRequest("ab", "test@test.com", "password123"),      // short username
                    new RegisterRequest("testuser", "not-an-email", "password123"), // bad email
                    new RegisterRequest("testuser", "test@test.com", "short"),      // short password
                    new RegisterRequest("", "", "")                                 // blank
            );
        }

        @Test
        @DisplayName("should return 409 when username exists")
        void shouldReturn409WhenUsernameExists() throws Exception {
            when(authService.register(any(), any()))
                    .thenThrow(new UserAlreadyExistsException("Username", "testuser"));

            RegisterRequest request = new RegisterRequest("testuser", "test@test.com", "password123");

            mockMvc.perform(post("/api/auth/register")
                            .header(secretHeaderName, secretToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("CONFLICT"));
        }

        @Test
        @DisplayName("should return 429 when rate limited")
        void shouldReturn429WhenRateLimited() throws Exception {
            when(authService.register(any(), any())).thenReturn(successResponse);

            RegisterRequest request = new RegisterRequest("testuser", "test@test.com", "password123");
            String body = objectMapper.writeValueAsString(request);

            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/api/auth/register")
                                .header(secretHeaderName, secretToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isCreated());
            }

            // 6-й запрос
            mockMvc.perform(post("/api/auth/register")
                            .header(secretHeaderName, secretToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.error").value("TOO_MANY_REQUESTS"));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("should return 200 on successful login")
        void shouldReturn200OnSuccess() throws Exception {
            when(authService.login(any(), any())).thenReturn(successResponse);

            String body = "{\"login\":\"testuser\",\"password\":\"password123\"}";

            mockMvc.perform(post("/api/auth/login")
                            .header(secretHeaderName, secretToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token"));
        }

        @Test
        @DisplayName("should return 422 on blank login")
        void shouldReturn422OnBlankLogin() throws Exception {
            String body = "{\"login\":\"\",\"password\":\"password123\"}";

            mockMvc.perform(post("/api/auth/login")
                            .header(secretHeaderName, secretToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("should return 429 when rate limited")
        void shouldReturn429WhenLoginRateLimited() throws Exception {
            when(authService.login(any(), any())).thenReturn(successResponse);

            String body = "{\"login\":\"testuser\",\"password\":\"password123\"}";

            for (int i = 0; i < 10; i++) {
                mockMvc.perform(post("/api/auth/login")
                                .header(secretHeaderName, secretToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isOk());
            }

            mockMvc.perform(post("/api/auth/login")
                            .header(secretHeaderName, secretToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isTooManyRequests());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout")
    class Logout {

        @Test
        @DisplayName("should return 401 without token")
        void shouldReturn401WithoutToken() throws Exception {
            mockMvc.perform(post("/api/auth/logout")
                            .header(secretHeaderName, secretToken))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/auth/me")
    class GetCurrentUser {

        @Test
        @DisplayName("should return 401 without token")
        void shouldReturn401WithoutToken() throws Exception {
            mockMvc.perform(get("/api/auth/me")
                            .header(secretHeaderName, secretToken))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturnUserWhenAuthorized() throws Exception {
             mockMvc.perform(get("/api/auth/me")
                            .header(secretHeaderName, secretToken)
                            .header("X-User-Id", "1")
                            .header("X-User-Role", "USER")
                            .header("X-User-Sub", "testuser"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/password/change")
    class ChangePassword {

        @Test
        @DisplayName("should return 204 when authorized with valid data")
        void shouldReturn200WhenAuthorized() throws Exception {
            String body = "{\"currentPassword\":\"old123456\",\"newPassword\":\"new123456\"}";

            mockMvc.perform(post("/api/auth/password/change")
                            .header(secretHeaderName, secretToken)
                            .header("X-User-Id", "1")
                            .header("X-User-Role", "USER")
                            .header("X-User-Sub", "testuser")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 422 on invalid new password")
        void shouldReturn422OnInvalidPassword() throws Exception {
            String body = "{\"currentPassword\":\"old123456\",\"newPassword\":\"short\"}";

            mockMvc.perform(post("/api/auth/password/change")
                            .header(secretHeaderName, secretToken)
                            .header("X-User-Id", "1")
                            .header("X-User-Role", "USER")
                            .header("X-User-Sub", "testuser")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("should return 401 without token")
        void shouldReturn401WithoutToken() throws Exception {
            String body = "{\"currentPassword\":\"old123456\",\"newPassword\":\"new123456\"}";

            mockMvc.perform(post("/api/auth/password/change")
                            .header(secretHeaderName, secretToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Internal security filter")
    class InternalSecurity {

        @Test
        @DisplayName("should return 403 without internal secret header")
        void shouldReturn403WithoutSecret() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new RegisterRequest("user", "a@b.com", "password123"))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 403 with wrong internal secret")
        void shouldReturn403WithWrongSecret() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .header(secretHeaderName, "wrong-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new RegisterRequest("user", "a@b.com", "password123"))))
                    .andExpect(status().isForbidden());
        }
    }
}
