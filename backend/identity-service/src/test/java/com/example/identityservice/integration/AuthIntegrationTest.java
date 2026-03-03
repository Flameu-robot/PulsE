package com.example.identityservice.integration;

import com.example.identityservice.dto.request.LoginRequest;
import com.example.identityservice.dto.request.RegisterRequest;
import com.example.identityservice.dto.response.AuthResponse;
import com.example.identityservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("full auth flow: register → login → refresh → logout")
    void fullAuthFlow() throws Exception {
        // 1. Register
        RegisterRequest registerRequest = new RegisterRequest("flowuser", "flow@test.com", "password123");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        AuthResponse registerResponse = objectMapper.readValue(
                registerResult.getResponse().getContentAsString(),
                AuthResponse.class
        );

        assertThat(registerResponse.username()).isEqualTo("flowuser");
        assertThat(registerResponse.role()).isEqualTo("USER");

        // 2. Login
        LoginRequest loginRequest = new LoginRequest("flowuser", "password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        AuthResponse loginResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                AuthResponse.class
        );

        // 3. Refresh
        String refreshBody = "{\"refreshToken\":\"" + loginResponse.refreshToken() + "\"}";

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        // 4. Logout
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + loginResponse.accessToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(2)
    @DisplayName("should not register with duplicate username")
    void shouldNotRegisterDuplicate() throws Exception {
        RegisterRequest request = new RegisterRequest("duplicate", "dup@test.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        RegisterRequest duplicateRequest = new RegisterRequest("duplicate", "other@test.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    @DisplayName("should not login with wrong password")
    void shouldNotLoginWithWrongPassword() throws Exception {
        RegisterRequest register = new RegisterRequest("wrongpass", "wrong@test.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest("wrongpass", "wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}
