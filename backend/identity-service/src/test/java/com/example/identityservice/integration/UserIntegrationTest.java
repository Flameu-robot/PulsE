package com.example.identityservice.integration;

import com.example.identityservice.config.RedisCleanup;
import com.example.identityservice.config.RedisTestContainerConfig;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "internal.security.token=test-secret-token",
        "internal.security.header-name=X-Internal-Secret"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserIntegrationTest extends RedisTestContainerConfig {

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

    @Test
    @DisplayName("Should get own profile")
    void shouldGetOwnProfile() throws Exception {
        AuthResponse response = registerUser("profileuser", "profile@test.com", "password123");

        mockMvc.perform(withSecret(get("/api/users/me"))
                        .header("Authorization", "Bearer " + response.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("profileuser"))
                .andExpect(jsonPath("$.email").value("profile@test.com"));
    }

    @Test
    @DisplayName("Should return 401 when getting profile without token")
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(withSecret(get("/api/users/me")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should update profile")
    void shouldUpdateProfile() throws Exception {
        AuthResponse response = registerUser("updateuser", "update@test.com", "password123");

        String updateBody = "{\"bio\":\"My new bio\",\"phone\":\"+71234567890\"}";

        mockMvc.perform(withSecret(patch("/api/users/me"))
                        .header("Authorization", "Bearer " + response.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("My new bio"))
                .andExpect(jsonPath("$.phone").value("+71234567890"))
                .andExpect(jsonPath("$.username").value("updateuser"));
    }

    @Test
    @DisplayName("should get public profile without sensitive data")
    void shouldGetPublicProfile() throws Exception {
        AuthResponse response = registerUser("publicuser", "public@test.com", "password123");

        mockMvc.perform(withSecret(get("/api/users/" + response.userId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("publicuser"))
                .andExpect(jsonPath("$.id").value(response.userId()))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    @DisplayName("should return 404 on non-existent user")
    void shouldReturn404OnNonExistentUser() throws Exception {
        mockMvc.perform(withSecret(get("/api/users/999999")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should delete account and prevent login")
    void shouldDeleteAccount() throws Exception {
        AuthResponse response = registerAndActivate("deleteuser", "delete@test.com", "password123");

        mockMvc.perform(withSecret(delete("/api/users/me"))
                        .header("Authorization", "Bearer " + response.accessToken()))
                .andExpect(status().isNoContent());

        String loginBody = "{\"login\":\"deleteuser\",\"password\":\"password123\"}";

        mockMvc.perform(withSecret(post("/api/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should return 401 when deleting without token")
    void shouldReturn401OnDeleteWithoutToken() throws Exception {
        mockMvc.perform(withSecret(delete("/api/users/me")))
                .andExpect(status().isUnauthorized());
    }
}