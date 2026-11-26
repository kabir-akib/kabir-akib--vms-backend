package com.example.demo.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
@DisplayName("Authentication & Security Integration Tests")
class AuthControllerIntegrationTest {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Valid credentials return Bearer token and user info")
    void login_ValidCredentials_ReturnsToken() throws Exception {
        String payload = """
                {
                    "email": "admin@starkgroup.com",
                    "password": "stark2026"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token", not(emptyOrNullString())))
                .andExpect(jsonPath("$.data.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.data.user.email", is("admin@starkgroup.com")))
                .andExpect(jsonPath("$.data.user.role", is("ADMIN")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Invalid password returns 401 Unauthorized")
    void login_InvalidPassword_Returns401() throws Exception {
        String payload = """
                {
                    "email": "admin@starkgroup.com",
                    "password": "wrong-password"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Invalid email or password")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Blank or invalid email returns 400 Bad Request")
    void login_InvalidInput_Returns400() throws Exception {
        String payload = """
                {
                    "email": "not-an-email",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles - Unauthenticated request returns 401 Unauthorized")
    void vehicles_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles - Invalid Bearer token returns 401 Unauthorized")
    void vehicles_WithInvalidToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token-value"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("Full Bearer token lifecycle: login -> access vehicles & /me -> logout -> token rejected")
    void fullBearerTokenLifecycle() throws Exception {
        String loginPayload = """
                {
                    "email": "admin@starkgroup.com",
                    "password": "stark2026"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        String body = loginResult.getResponse().getContentAsString();
        Matcher matcher = TOKEN_PATTERN.matcher(body);
        if (!matcher.find()) {
            throw new AssertionError("Expected token in login response: " + body);
        }
        String token = matcher.group(1);

        // Protected vehicle endpoint succeeds with Bearer token
        mockMvc.perform(get("/api/v1/vehicles")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // /me endpoint succeeds with Bearer token
        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email", is("admin@starkgroup.com")));

        // Logout revokes token
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Revoked token is rejected
        mockMvc.perform(get("/api/v1/vehicles")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/health - Public health endpoint remains accessible without authentication")
    void healthEndpoint_RemainsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }
}
