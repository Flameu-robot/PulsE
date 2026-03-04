package com.example.identityservice.integration;

import com.example.identityservice.dto.request.LoginRequest;
import com.example.identityservice.dto.request.RegisterRequest;
import com.example.identityservice.dto.response.AuthResponse;
import com.example.identityservice.entity.User;
import com.example.identityservice.entity.enums.UserStatus;
import com.example.identityservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        objectMapper.registerModule(new JavaTimeModule());

        userRepository.deleteAll();
    }

    private void activateUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Test
    @Order(1)
    @DisplayName("full auth flow: register → login → refresh → logout")
    void fullAuthFlow() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("flowuser", "flow@test.com", "password123");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        activateUser("flowuser");

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

        String refreshBody = "{\"refreshToken\":\"" + loginResponse.refreshToken() + "\"}";

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

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

        activateUser("wrongpass");

        LoginRequest login = new LoginRequest("wrongpass", "wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    @DisplayName("should login with email")
    void shouldLoginWithEmail() throws Exception {
        RegisterRequest register = new RegisterRequest("emailuser", "email@test.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        activateUser("emailuser");

        String loginBody = "{\"login\":\"email@test.com\",\"password\":\"password123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("emailuser"));
    }

    @Test
    @Order(5)
    @DisplayName("should get current user")
    void shouldGetCurrentUser() throws Exception {
        RegisterRequest register = new RegisterRequest("meuser", "me@test.com", "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + response.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("meuser"))
                .andExpect(jsonPath("$.email").value("me@test.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @Order(6)
    @DisplayName("should change password")
    void shouldChangePassword() throws Exception {
        RegisterRequest register = new RegisterRequest("changepassuser", "changepass@test.com", "oldPassword123");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );

        activateUser("changepassuser");

        String changeBody = "{\"currentPassword\":\"oldPassword123\",\"newPassword\":\"newPassword123\"}";

        mockMvc.perform(post("/api/auth/password/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changeBody)
                        .header("Authorization", "Bearer " + response.accessToken()))
                .andExpect(status().isNoContent());

        String loginBody = "{\"login\":\"changepassuser\",\"password\":\"newPassword123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk());

        String oldLoginBody = "{\"login\":\"changepassuser\",\"password\":\"oldPassword123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oldLoginBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(7)
    @DisplayName("should get own profile")
    void shouldGetOwnProfile() throws Exception {
        RegisterRequest register = new RegisterRequest("profileuser", "profile@test.com", "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class
        );

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + response.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("profileuser"))
                .andExpect(jsonPath("$.email").value("profile@test.com"));
    }

    @Test
    @Order(8)
    @DisplayName("should update profile")
    void shouldUpdateProfile() throws Exception {
        RegisterRequest register = new RegisterRequest("updateuser", "update@test.com", "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class
        );

        String updateBody = "{\"bio\":\"My new bio\",\"phone\":\"+71234567890\"}";

        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody)
                        .header("Authorization", "Bearer " + response.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("My new bio"))
                .andExpect(jsonPath("$.phone").value("+71234567890"))
                .andExpect(jsonPath("$.username").value("updateuser"));
    }

    @Test
    @Order(9)
    @DisplayName("should get public profile")
    void shouldGetPublicProfile() throws Exception {
        RegisterRequest register = new RegisterRequest("publicuser", "public@test.com", "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class
        );

        mockMvc.perform(get("/api/users/" + response.userId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("publicuser"))
                .andExpect(jsonPath("$.id").value(response.userId()))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    @Order(10)
    @DisplayName("should delete account")
    void shouldDeleteAccount() throws Exception {
        RegisterRequest register = new RegisterRequest("deleteuser", "delete@test.com", "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class
        );

        activateUser("deleteuser");

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + response.accessToken()))
                .andExpect(status().isNoContent());

        String loginBody = "{\"login\":\"deleteuser\",\"password\":\"password123\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized());
    }
}