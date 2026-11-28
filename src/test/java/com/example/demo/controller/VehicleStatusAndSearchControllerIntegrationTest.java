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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
@DisplayName("Vehicle Status & Search API Integration Tests")
class VehicleStatusAndSearchControllerIntegrationTest {

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
    @DisplayName("Status update and search endpoints reject unauthenticated requests with 401")
    void endpoints_RequireAuthentication() throws Exception {
        mockMvc.perform(patch("/api/v1/vehicles/VMS-01/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/vehicles/search").param("vehicleNumber", "STK"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@starkgroup.com", roles = "ADMIN")
    @DisplayName("PATCH /api/v1/vehicles/{id}/status updates to ACTIVE, INACTIVE, MAINTENANCE and persists")
    void updateStatus_AllValidStatuses_Persisted() throws Exception {
        for (String targetStatus : new String[]{"MAINTENANCE", "INACTIVE", "ACTIVE"}) {
            mockMvc.perform(patch("/api/v1/vehicles/VMS-01/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"" + targetStatus + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.status", is(targetStatus)));

            mockMvc.perform(get("/api/v1/vehicles/VMS-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status", is(targetStatus)));
        }
    }

    @Test
    @WithMockUser(username = "admin@starkgroup.com", roles = "ADMIN")
    @DisplayName("PATCH /api/v1/vehicles/{id}/status rejects invalid status and unknown vehicle")
    void updateStatus_InvalidStatusOrUnknownVehicle() throws Exception {
        mockMvc.perform(patch("/api/v1/vehicles/VMS-01/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BROKEN_UNKNOWN\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/v1/vehicles/UNKNOWN-999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@starkgroup.com", roles = "ADMIN")
    @DisplayName("GET /api/v1/vehicles/search supports exact, partial, case-insensitive, combined criteria, and empty results")
    void searchVehicles_AllCriteria() throws Exception {
        // Exact & partial case-insensitive vehicleNumber
        mockMvc.perform(get("/api/v1/vehicles/search").param("vehicleNumber", "stk-1042"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is("VMS-01")));

        mockMvc.perform(get("/api/v1/vehicles/search").param("vehicleNumber", "104"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));

        // Company, Type, Brand, Model (case-insensitive partial)
        mockMvc.perform(get("/api/v1/vehicles/search")
                        .param("company", "stark")
                        .param("type", "TRUCK")
                        .param("brand", "scania")
                        .param("model", "r500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].brand", is("Scania")));

        // No match returns empty list
        mockMvc.perform(get("/api/v1/vehicles/search").param("vehicleNumber", "NO-SUCH-VEHICLE-XYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", empty()));
    }
}
