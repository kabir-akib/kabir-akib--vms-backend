package com.example.demo.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
@DisplayName("Vehicle Pagination & Driver CRUD Integration Tests")
class VehiclePaginationAndDriverIntegrationTest {

    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");

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
    @WithMockUser(username = "admin@starkgroup.com", roles = "ADMIN")
    @DisplayName("GET /api/v1/vehicles?page=... returns paginated, filtered, sorted results")
    void vehiclePaginationAndFiltering() throws Exception {
        // First page of 3 items
        mockMvc.perform(get("/api/v1/vehicles")
                        .param("page", "0")
                        .param("size", "3")
                        .param("sortBy", "vehicleNumber")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(3)))
                .andExpect(jsonPath("$.data.totalElements", greaterThanOrEqualTo(9)))
                .andExpect(jsonPath("$.data.hasNext", is(true)))
                .andExpect(jsonPath("$.data.hasPrevious", is(false)));

        // Second page
        mockMvc.perform(get("/api/v1/vehicles")
                        .param("page", "1")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page", is(1)))
                .andExpect(jsonPath("$.data.hasPrevious", is(true)));

        // Combined filter
        mockMvc.perform(get("/api/v1/vehicles")
                        .param("page", "0")
                        .param("size", "10")
                        .param("company", "stark")
                        .param("type", "TRUCK")
                        .param("brand", "scania"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));

        // Empty result
        mockMvc.perform(get("/api/v1/vehicles")
                        .param("page", "0")
                        .param("size", "10")
                        .param("vehicleNumber", "NON-EXISTENT-999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", empty()))
                .andExpect(jsonPath("$.data.totalElements", is(0)));

        // Invalid sortBy / page
        mockMvc.perform(get("/api/v1/vehicles")
                        .param("page", "0")
                        .param("sortBy", "invalidField"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/vehicles")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Driver endpoints reject unauthenticated requests with 401")
    void driverEndpoints_RequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/drivers"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@starkgroup.com", roles = "ADMIN")
    @DisplayName("Full Driver CRUD lifecycle, duplicate licence rejection, and validation")
    void driverCrudLifecycle() throws Exception {
        String futureDate = LocalDate.now().plusYears(2).toString();
        String createPayload = """
                {
                  "name": "Tony Stark",
                  "phoneNumber": "+8801711223344",
                  "drivingLicenceNumber": "DL-STARK-001",
                  "licenceExpiryDate": "%s",
                  "address": "Malibu Point 10880",
                  "status": "ACTIVE"
                }
                """.formatted(futureDate);

        // Create
        MvcResult createResult = mockMvc.perform(post("/api/v1/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.drivingLicenceNumber", is("DL-STARK-001")))
                .andReturn();
        String driverId = extractId(createResult.getResponse().getContentAsString());

        // Duplicate licence rejected
        mockMvc.perform(post("/api/v1/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isBadRequest());

        // List & Get by ID
        mockMvc.perform(get("/api/v1/drivers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(get("/api/v1/drivers/" + driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("Tony Stark")));

        // Update
        String updatePayload = """
                {
                  "name": "Tony Stark Updated",
                  "phoneNumber": "+8801711223344",
                  "drivingLicenceNumber": "DL-STARK-001",
                  "licenceExpiryDate": "%s",
                  "address": "Stark Tower",
                  "status": "INACTIVE"
                }
                """.formatted(futureDate);
        mockMvc.perform(put("/api/v1/drivers/" + driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("INACTIVE")));

        // Delete
        mockMvc.perform(delete("/api/v1/drivers/" + driverId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/drivers/" + driverId))
                .andExpect(status().isNotFound());
    }

    private String extractId(String json) {
        Matcher matcher = ID_PATTERN.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalStateException("Could not extract id from response: " + json);
    }
}
