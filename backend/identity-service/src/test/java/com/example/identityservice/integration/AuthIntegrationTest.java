package com.example.identityservice.integration;

import com.example.identityservice.config.RedisCleanup;
import com.example.identityservice.config.RedisTestContainerConfig;
import com.example.identityservice.dto.request.LoginRequest;
import com.example.identityservice.dto.request.RegisterRequest;
import com.example.identityservice.dto.response.AuthResponse;
import com.example.identityservice.entity.User;
import com.example.identityservice.entity.enums.UserStatus;
import com.example.identityservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "gateway.security.token=test-secret-token",
        "gateway.security.header-name=X-Internal-Secret"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest extends RedisTestContainerConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisCleanup redisCleanup;

    @Autowired
    private UserRepository userRepository;

    @Value("${internal.security.header-name}")
    private String secretHeaderName;

    @Value("${internal.security.token}")
    private String secretToken;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        userRepository.deleteAll();
        redisCleanup.flushAll();
    }

    // -- Helpers --
    private MockHttpServletRequestBuilder withSecret(MockHttpServletRequestBuilder builder) {
        return builder.header(secretHeaderName, secretToken);
    }

    private MockHttpServletRequestBuilder withAuthenticatedUser(
            MockHttpServletRequestBuilder builder,
            Long userId,
            String username,
            String role) {
        return builder
                .header(secretHeaderName, secretToken)
                .header("X-User-Id", String.valueOf(userId))
                .header("X-User-Role", role)
                .header("X-User-Sub", username);
    }

    private void activateUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    private AuthResponse registerUser(String username, String email, String password) throws Exception {
        RegisterRequest request = new RegisterRequest(username, email, password);

        MvcResult result = mockMvc.perform(withSecret(post("/api/auth/register"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );
    }

    private AuthResponse registerAndActivate(String username, String email, String password) throws Exception {
        AuthResponse response = registerUser(username, email, password);
        activateUser(username);
        return response;
    }

    private AuthResponse loginUser(String login, String password) throws Exception {
        LoginRequest request = new LoginRequest(login, password);

        MvcResult result = mockMvc.perform(withSecret(post("/api/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );
    }

    @Test
    @DisplayName("Full Auth flow: Register -> Activate -> Login -> Refresh -> Logout")
    void fullAuthFlow() throws Exception {
        registerUser("flowuser", "flow@test.com", "password123");
        activateUser("flowuser");

        AuthResponse loginResponse = loginUser("flowuser", "password123");

        // refresh
        mockMvc.perform(withSecret(post("/api/auth/refresh"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + loginResponse.refreshToken() + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        // logout
        mockMvc.perform(withAuthenticatedUser(
                        post("/api/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"refreshToken\":\"" + loginResponse.refreshToken() + "\"}"),
                        loginResponse.userId(), loginResponse.username(), loginResponse.role()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should not register with duplicate username")
    void shouldNotRegisterDuplicateUsername() throws Exception {
        registerUser("duplicate", "dup@test.com", "password123");

        RegisterRequest duplicateRequest = new RegisterRequest("duplicate", "other@test.com", "password123");

        mockMvc.perform(withSecret(post("/api/auth/register"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should not register with duplicate email")
    void shouldNotRegisterDuplicateEmail() throws Exception {
        registerUser("user1", "same@test.com", "password123");

        RegisterRequest duplicateRequest = new RegisterRequest("user2", "same@test.com", "password123");

        mockMvc.perform(withSecret(post("/api/auth/register"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should not login with wrong password")
    void shouldNotLoginWithWrongPassword() throws Exception {
        registerAndActivate("wrong_pass", "wrong@test.com", "password123");

        LoginRequest login = new LoginRequest("wrong_pass", "wrong_password");

        mockMvc.perform(withSecret(post("/api/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 on login with non-existent user")
    void shouldReturn401OnNonExistentUser() throws Exception {
        String body = "{\"login\":\"ghost\",\"password\":\"password123\"}";

        mockMvc.perform(withSecret(post("/api/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should login with email")
    void shouldLoginWithEmail() throws Exception {
        registerAndActivate("emailuser", "email@test.com", "password123");

        String body = "{\"login\":\"email@test.com\",\"password\":\"password123\"}";

        mockMvc.perform(withSecret(post("/api/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("emailuser"));
    }

    @Test
    @DisplayName("Should return 401 on refresh with invalid token")
    void shouldReturn401OnInvalidRefreshToken() throws Exception {
        String body = "{\"refreshToken\":\"invalid-token\"}";

        mockMvc.perform(withSecret(post("/api/auth/refresh"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should not refresh after logout")
    void shouldNotRefreshAfterLogout() throws Exception {
        registerAndActivate("refreshuser", "refresh@test.com", "password123");
        AuthResponse response = loginUser("refreshuser", "password123");

        // logout
        mockMvc.perform(withAuthenticatedUser(
                        post("/api/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"refreshToken\":\"" + response.refreshToken() + "\"}"),
                        response.userId(), response.username(), response.role()))
                .andExpect(status().isNoContent());

        // refresh
        mockMvc.perform(withSecret(post("/api/auth/refresh"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + response.refreshToken() + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should get current user")
    void shouldGetCurrentUser() throws Exception {
        registerUser("meuser", "me@test.com", "password123");

        mockMvc.perform(withAuthenticatedUser(
                        get("/api/auth/me"), 1L, "meuser", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("meuser"))
                .andExpect(jsonPath("$.email").value("me@test.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("Should change password and login with new one")
    void shouldChangePassword() throws Exception {
        AuthResponse response = registerAndActivate(
                "changeuser", "change@test.com", "oldPassword123");

        String changeBody = "{\"currentPassword\":\"oldPassword123\",\"newPassword\":\"newPassword123\"}";

        // change password
        mockMvc.perform(withAuthenticatedUser(
                        post("/api/auth/password/change")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(changeBody),
                        response.userId(), response.username(), response.role()))
                .andExpect(status().isNoContent());

        // login с новым паролем
        mockMvc.perform(withSecret(post("/api/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"changeuser\",\"password\":\"newPassword123\"}"))
                .andExpect(status().isOk());

        // login со старым паролем
        mockMvc.perform(withSecret(post("/api/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"changeuser\",\"password\":\"oldPassword123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should logout-all and revoke all sessions")
    void shouldLogoutAll() throws Exception {
        registerAndActivate("logoutalluser", "logoutall@test.com", "password123");

        AuthResponse session1 = loginUser("logoutalluser", "password123");
        AuthResponse session2 = loginUser("logoutalluser", "password123");

        // logout-all через Gateway хедеры
        mockMvc.perform(withAuthenticatedUser(
                        post("/api/auth/logout-all"),
                        session1.userId(), session1.username(), session1.role()))
                .andExpect(status().isNoContent());

        // оба refresh токена должны быть отозваны
        mockMvc.perform(withSecret(post("/api/auth/refresh"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + session1.refreshToken() + "\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(withSecret(post("/api/auth/refresh"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + session2.refreshToken() + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}