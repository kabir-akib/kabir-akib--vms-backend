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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
@DisplayName("Vehicle Maintenance API Integration Tests")
class VehicleMaintenanceControllerIntegrationTest {

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
    @DisplayName("Maintenance endpoints reject unauthenticated requests with 401 Unauthorized")
    void maintenanceEndpoints_RequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/VMS-01/maintenance"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/vehicles/VMS-01/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@starkgroup.com", roles = "ADMIN")
    @DisplayName("Full maintenance CRUD lifecycle and newest-first history ordering")
    void fullMaintenanceLifecycleAndHistoryOrdering() throws Exception {
        // 1. Create older record
        String olderPayload = """
                {
                  "maintenanceDate": "2026-10-01",
                  "maintenanceType": "OIL_CHANGE",
                  "cost": 120.00,
                  "vendorName": "City Auto",
                  "notes": "First oil change"
                }
                """;
        MvcResult olderResult = mockMvc.perform(post("/api/v1/vehicles/VMS-01/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(olderPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.vehicleId", is("VMS-01")))
                .andReturn();
        String olderId = extractId(olderResult.getResponse().getContentAsString());

        // 2. Create newer record
        String newerPayload = """
                {
                  "maintenanceDate": "2026-11-15",
                  "maintenanceType": "REPAIR",
                  "cost": 450.50,
                  "vendorName": "Stark Service Center",
                  "notes": "Brake pad replacement"
                }
                """;
        MvcResult newerResult = mockMvc.perform(post("/api/v1/vehicles/VMS-01/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newerPayload))
                .andExpect(status().isCreated())
                .andReturn();
        String newerId = extractId(newerResult.getResponse().getContentAsString());

        // 3. Create record on another vehicle (VMS-02) to verify isolation
        mockMvc.perform(post("/api/v1/vehicles/VMS-02/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(olderPayload))
                .andExpect(status().isCreated());

        // 4. History for VMS-01 returns only VMS-01 records, newest first
        mockMvc.perform(get("/api/v1/vehicles/VMS-01/maintenance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id", is(newerId)))
                .andExpect(jsonPath("$.data[1].id", is(olderId)));

        // 5. Get single record
        mockMvc.perform(get("/api/v1/vehicles/VMS-01/maintenance/" + newerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.vendorName", is("Stark Service Center")));

        // 6. Cross-vehicle access is rejected with 404
        mockMvc.perform(get("/api/v1/vehicles/VMS-02/maintenance/" + newerId))
                .andExpect(status().isNotFound());

        // 7. Update record
        String updatePayload = """
                {
                  "maintenanceDate": "2026-11-16",
                  "maintenanceType": "INSPECTION",
                  "cost": 200.00,
                  "vendorName": "Stark Service Center",
                  "notes": "Updated inspection"
                }
                """;
        mockMvc.perform(put("/api/v1/vehicles/VMS-01/maintenance/" + newerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maintenanceType", is("INSPECTION")))
                .andExpect(jsonPath("$.data.cost", is(200.00)));

        // 8. Delete record
        mockMvc.perform(delete("/api/v1/vehicles/VMS-01/maintenance/" + olderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        mockMvc.perform(get("/api/v1/vehicles/VMS-01/maintenance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "admin@starkgroup.com", roles = "ADMIN")
    @DisplayName("Validation rejects negative cost and missing required fields with 400 Bad Request")
    void createMaintenance_ValidationErrors_Returns400() throws Exception {
        String invalidPayload = """
                {
                  "maintenanceDate": null,
                  "maintenanceType": "REPAIR",
                  "cost": -10.00,
                  "vendorName": ""
                }
                """;

        mockMvc.perform(post("/api/v1/vehicles/VMS-01/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@starkgroup.com", roles = "ADMIN")
    @DisplayName("Unknown vehicle and unknown maintenance record return 404 Not Found")
    void unknownVehicleOrRecord_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/UNKNOWN-999/maintenance"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/vehicles/VMS-01/maintenance/unknown-maint-id"))
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
