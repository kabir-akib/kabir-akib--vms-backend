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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
@WithMockUser(username = "admin@starkgroup.com", roles = "ADMIN")
@DisplayName("Vehicle Controller Full CRUD Integration Tests")
class VehicleControllerIntegrationTest {

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
    @DisplayName("POST /api/v1/vehicles - Successfully creates vehicle with generated ID")
    void createVehicle_Success() throws Exception {
        String payload = """
                {
                    "vehicleNumber": "TEST-INT-001",
                    "company": "Integration Transport Ltd",
                    "type": "TRUCK",
                    "brand": "Scania",
                    "model": "540S Highline",
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Vehicle registered successfully")))
                .andExpect(jsonPath("$.data.id", not(emptyOrNullString())))
                .andExpect(jsonPath("$.data.vehicleNumber", is("TEST-INT-001")))
                .andExpect(jsonPath("$.data.company", is("Integration Transport Ltd")))
                .andExpect(jsonPath("$.data.type", is("TRUCK")))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("POST /api/v1/vehicles - Duplicate vehicle number returns 409 Conflict")
    void createVehicle_DuplicateNumber_Returns409() throws Exception {
        // "STK-1042" is seeded in the test database
        String duplicatePayload = """
                {
                    "vehicleNumber": "STK-1042",
                    "company": "Another Company",
                    "type": "VAN",
                    "brand": "Ford",
                    "model": "Transit"
                }
                """;

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicatePayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Duplicate Vehicle Number")))
                .andExpect(jsonPath("$.message", is("Vehicle with number 'STK-1042' already exists in the fleet.")));
    }

    @Test
    @DisplayName("POST /api/v1/vehicles - Validation errors return 400 Bad Request")
    void createVehicle_ValidationFailure_Returns400() throws Exception {
        String invalidPayload = """
                {
                    "vehicleNumber": "",
                    "company": "",
                    "type": null,
                    "brand": "",
                    "model": ""
                }
                """;

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles - Lists seeded vehicles")
    void getAllVehicles_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(9))));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles - Filters by query string")
    void getAllVehicles_Filtered_ReturnsMatching() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles").param("query", "Scania"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].brand", is("Scania")));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles/{id} - Returns existing vehicle by ID")
    void getVehicleById_Found_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/VMS-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is("VMS-01")))
                .andExpect(jsonPath("$.data.vehicleNumber", is("STK-1042")));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles/{id} - Non-existent ID returns 404 Not Found")
    void getVehicleById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/UNKNOWN-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("PUT /api/v1/vehicles/{id} - Successfully updates an existing vehicle")
    void updateVehicle_Success_Returns200() throws Exception {
        String updatePayload = """
                {
                    "vehicleNumber": "STK-1042",
                    "company": "Stark Logistics Updated",
                    "type": "TRUCK",
                    "brand": "Scania",
                    "model": "R500 V8 Modified",
                    "status": "MAINTENANCE"
                }
                """;

        mockMvc.perform(put("/api/v1/vehicles/VMS-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.company", is("Stark Logistics Updated")))
                .andExpect(jsonPath("$.data.status", is("MAINTENANCE")));
    }

    @Test
    @DisplayName("PUT /api/v1/vehicles/{id} - Duplicate vehicle number from another vehicle returns 409 Conflict")
    void updateVehicle_DuplicateOtherVehicleNumber_Returns409() throws Exception {
        // VMS-02 has vehicleNumber STK-2019
        String conflictPayload = """
                {
                    "vehicleNumber": "STK-2019",
                    "company": "Stark Logistics",
                    "type": "TRUCK",
                    "brand": "Scania",
                    "model": "R500",
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(put("/api/v1/vehicles/VMS-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conflictPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Duplicate Vehicle Number")));
    }

    @Test
    @DisplayName("DELETE /api/v1/vehicles/{id} - Deletes existing vehicle")
    void deleteVehicle_Success_Returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/vehicles/VMS-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Vehicle deleted successfully")));

        // Confirm it is now gone
        mockMvc.perform(get("/api/v1/vehicles/VMS-01"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/vehicles/{id} - Non-existent ID returns 404 Not Found")
    void deleteVehicle_NotFound_Returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/vehicles/UNKNOWN-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }
}
