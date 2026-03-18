package com.paypalclone.featheredoofbird.identity.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paypalclone.featheredoofbird.identity.application.AuthService;
import com.paypalclone.featheredoofbird.identity.application.DuplicateEmailException;
import com.paypalclone.featheredoofbird.identity.application.InvalidCredentialsException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

class AuthControllerTest {

    private MockMvc mockMvc;
    private AuthService authService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        objectMapper =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc =
                MockMvcBuilders.standaloneSetup(new AuthController(authService))
                        .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                        .build();
    }

    private AuthService.AuthResult sampleAuthResult() {
        return new AuthService.AuthResult(
                "jwt-token",
                "Bearer",
                Instant.parse("2026-12-31T23:59:59Z"),
                new AuthService.AuthenticatedUser(1L, "user@example.com", "PERSONAL", "ACTIVE"));
    }

    @Test
    void register_returnsCreatedWithAuthResult() throws Exception {
        when(authService.register(any(), any(), any())).thenReturn(sampleAuthResult());

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"email\":\"user@example.com\","
                                                + "\"password\":\"password123\","
                                                + "\"role\":\"PERSONAL\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void register_returns409OnDuplicateEmail() throws Exception {
        when(authService.register(any(), any(), any()))
                .thenThrow(new DuplicateEmailException("user@example.com"));

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"email\":\"user@example.com\","
                                                + "\"password\":\"password123\","
                                                + "\"role\":\"PERSONAL\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void login_returnsOkWithAuthResult() throws Exception {
        when(authService.login(any(), any())).thenReturn(sampleAuthResult());

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"email\":\"user@example.com\","
                                                + "\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.user.email").value("user@example.com"));
    }

    @Test
    void login_returns401OnInvalidCredentials() throws Exception {
        when(authService.login(any(), any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"email\":\"user@example.com\","
                                                + "\"password\":\"wrongpassword\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void login_passesNormalizedEmailToService() throws Exception {
        when(authService.login(any(), any())).thenReturn(sampleAuthResult());

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"email\":\"USER@EXAMPLE.COM\","
                                                + "\"password\":\"password123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void register_userRoleIncludedInResult() throws Exception {
        AuthService.AuthResult result =
                new AuthService.AuthResult(
                        "tok",
                        "Bearer",
                        Instant.parse("2026-12-31T23:59:59Z"),
                        new AuthService.AuthenticatedUser(
                                2L, "biz@example.com", "BUSINESS", "ACTIVE"));
        when(authService.register(any(), any(), any())).thenReturn(result);

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"email\":\"biz@example.com\","
                                                + "\"password\":\"password123\","
                                                + "\"role\":\"BUSINESS\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.role").value("BUSINESS"));
    }
}
