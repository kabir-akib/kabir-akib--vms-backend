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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
@DisplayName("Assignment & Dashboard API Integration Tests")
class AssignmentAndDashboardIntegrationTest {

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
    @DisplayName("Assignment and Dashboard endpoints reject unauthenticated requests with 401")
    void endpoints_RequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/assignments"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@starkgroup.com", roles = "ADMIN")
    @DisplayName("Full assignment lifecycle: assign -> conflict checks -> end -> history preserved")
    void assignmentLifecycleAndConflictChecks() throws Exception {
        String driverId = createTestDriver("DL-ASGN-001");
        String assignPayload = """
                {
                  "driverId": "%s",
                  "vehicleId": "VMS-01",
                  "assignedDate": "%s"
                }
                """.formatted(driverId, LocalDate.now());

        // 1. Create assignment
        MvcResult createResult = mockMvc.perform(post("/api/v1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")))
                .andReturn();
        String assignmentId = extractId(createResult.getResponse().getContentAsString());

        // 2. Duplicate / simultaneous assignment rejected
        mockMvc.perform(post("/api/v1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignPayload))
                .andExpect(status().isBadRequest());

        // 3. End assignment
        mockMvc.perform(patch("/api/v1/assignments/" + assignmentId + "/end"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("INACTIVE")));

        // 4. Repeated end rejected
        mockMvc.perform(patch("/api/v1/assignments/" + assignmentId + "/end"))
                .andExpect(status().isBadRequest());

        // 5. History preserved for both vehicle and driver
        mockMvc.perform(get("/api/v1/vehicles/VMS-01/assignments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(get("/api/v1/drivers/" + driverId + "/assignments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].status", is("INACTIVE")));
    }

    @Test
    @WithMockUser(username = "admin@starkgroup.com", roles = "ADMIN")
    @DisplayName("GET /api/v1/dashboard/summary returns structured fleet summary")
    void dashboardSummary_ReturnsStructuredData() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.vehicles.totalVehicles", greaterThanOrEqualTo(9)))
                .andExpect(jsonPath("$.data.documents.totalDocuments", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.maintenance.totalRecords", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.drivers.totalDrivers", greaterThanOrEqualTo(0)));
    }

    private String createTestDriver(String licence) throws Exception {
        String payload = """
                {
                  "name": "Natasha Romanoff",
                  "phoneNumber": "+8801700000000",
                  "drivingLicenceNumber": "%s",
                  "licenceExpiryDate": "%s",
                  "address": "Avengers Compound",
                  "status": "ACTIVE"
                }
                """.formatted(licence, LocalDate.now().plusYears(2));

        MvcResult result = mockMvc.perform(post("/api/v1/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();
        return extractId(result.getResponse().getContentAsString());
    }

    private String extractId(String json) {
        Matcher matcher = ID_PATTERN.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalStateException("Could not extract id from response: " + json);
    }
}
