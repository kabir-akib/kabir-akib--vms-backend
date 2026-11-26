package com.example.demo.controller;

import com.example.demo.dto.request.VehicleCreateRequest;
import com.example.demo.dto.request.VehicleUpdateRequest;
import com.example.demo.dto.response.FleetMetricsResponse;
import com.example.demo.dto.response.VehicleResponse;
import com.example.demo.exception.DuplicateVehicleNumberException;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.enums.VehicleStatus;
import com.example.demo.model.enums.VehicleType;
import com.example.demo.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private VehicleController vehicleController;

    private VehicleResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(vehicleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleResponse = new VehicleResponse();
        sampleResponse.setId("VMS-01");
        sampleResponse.setVehicleNumber("STK-1042");
        sampleResponse.setCompany("Stark Logistics");
        sampleResponse.setType(VehicleType.TRUCK);
        sampleResponse.setTypeLabel("Truck");
        sampleResponse.setBrand("Scania");
        sampleResponse.setModel("R500 V8 Highline");
        sampleResponse.setDisplayName("Scania R500 V8 Highline");
        sampleResponse.setStatus(VehicleStatus.ACTIVE);
        sampleResponse.setStatusLabel("Active");
        sampleResponse.setCreatedAt(Instant.now());
        sampleResponse.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("POST /api/v1/vehicles - Should register vehicle and return 201 Created")
    void registerVehicle_Valid_Returns201() throws Exception {
        String jsonPayload = """
                {
                    "id": "VMS-01",
                    "vehicleNumber": "STK-1042",
                    "company": "Stark Logistics",
                    "type": "TRUCK",
                    "brand": "Scania",
                    "model": "R500 V8 Highline",
                    "status": "ACTIVE"
                }
                """;

        when(vehicleService.createVehicle(any(VehicleCreateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Vehicle registered successfully")))
                .andExpect(jsonPath("$.data.id", is("VMS-01")))
                .andExpect(jsonPath("$.data.vehicleNumber", is("STK-1042")));
    }

    @Test
    @DisplayName("POST /api/v1/vehicles - Duplicate vehicle number should return 409 Conflict")
    void registerVehicle_DuplicateNumber_Returns409() throws Exception {
        String jsonPayload = """
                {
                    "vehicleNumber": "STK-1042",
                    "company": "Stark Logistics",
                    "type": "TRUCK",
                    "brand": "Scania",
                    "model": "R500 V8 Highline"
                }
                """;

        when(vehicleService.createVehicle(any(VehicleCreateRequest.class)))
                .thenThrow(new DuplicateVehicleNumberException("STK-1042"));

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Duplicate Vehicle Number")))
                .andExpect(jsonPath("$.message", is("Vehicle with number 'STK-1042' already exists in the fleet.")));
    }

    @Test
    @DisplayName("POST /api/v1/vehicles - Validation error should return 400 Bad Request")
    void registerVehicle_Invalid_Returns400() throws Exception {
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
    @DisplayName("GET /api/v1/vehicles - Should return list of vehicles")
    void getAllVehicles_Returns200() throws Exception {
        when(vehicleService.getVehicles(null, null, null)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].vehicleNumber", is("STK-1042")));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles/{id} - Should return vehicle when exists")
    void getVehicleById_Found_Returns200() throws Exception {
        when(vehicleService.getVehicleById("VMS-01")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/vehicles/VMS-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is("VMS-01")));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles/{id} - Should return 404 Not Found when not exists")
    void getVehicleById_NotFound_Returns404() throws Exception {
        when(vehicleService.getVehicleById("UNKNOWN"))
                .thenThrow(new ResourceNotFoundException("Vehicle", "id", "UNKNOWN"));

        mockMvc.perform(get("/api/v1/vehicles/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("PUT /api/v1/vehicles/{id} - Should update and return 200 OK")
    void updateVehicle_Success_Returns200() throws Exception {
        String updatePayload = """
                {
                    "vehicleNumber": "STK-1042",
                    "company": "Stark Logistics",
                    "type": "TRUCK",
                    "brand": "Scania",
                    "model": "R500 V8 Highline",
                    "status": "MAINTENANCE"
                }
                """;

        sampleResponse.setStatus(VehicleStatus.MAINTENANCE);
        when(vehicleService.updateVehicle(eq("VMS-01"), any(VehicleUpdateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/vehicles/VMS-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("MAINTENANCE")));
    }

    @Test
    @DisplayName("PUT /api/v1/vehicles/{id} - Duplicate vehicle number should return 409 Conflict")
    void updateVehicle_DuplicateNumber_Returns409() throws Exception {
        String updatePayload = """
                {
                    "vehicleNumber": "STK-2019",
                    "company": "Stark Logistics",
                    "type": "TRUCK",
                    "brand": "Scania",
                    "model": "R500 V8 Highline",
                    "status": "MAINTENANCE"
                }
                """;

        when(vehicleService.updateVehicle(eq("VMS-01"), any(VehicleUpdateRequest.class)))
                .thenThrow(new DuplicateVehicleNumberException("STK-2019"));

        mockMvc.perform(put("/api/v1/vehicles/VMS-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Duplicate Vehicle Number")))
                .andExpect(jsonPath("$.message", is("Vehicle with number 'STK-2019' already exists in the fleet.")));
    }

    @Test
    @DisplayName("DELETE /api/v1/vehicles/{id} - Should delete and return 200 OK")
    void deleteVehicle_Success_Returns200() throws Exception {
        doNothing().when(vehicleService).deleteVehicle("VMS-01");

        mockMvc.perform(delete("/api/v1/vehicles/VMS-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Vehicle deleted successfully")));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles/metrics - Should return KPI metrics")
    void getFleetMetrics_Returns200() throws Exception {
        FleetMetricsResponse metrics = new FleetMetricsResponse(9, 6, 2, 1);
        when(vehicleService.getFleetMetrics()).thenReturn(metrics);

        mockMvc.perform(get("/api/v1/vehicles/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalVehicles", is(9)))
                .andExpect(jsonPath("$.data.activeVehicles", is(6)))
                .andExpect(jsonPath("$.data.maintenanceVehicles", is(2)))
                .andExpect(jsonPath("$.data.inactiveVehicles", is(1)));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles/companies - Should return company list")
    void getAvailableCompanies_Returns200() throws Exception {
        when(vehicleService.getAvailableCompanies()).thenReturn(List.of("Stark Logistics", "Stark Distribution"));

        mockMvc.perform(get("/api/v1/vehicles/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0]", is("Stark Logistics")));
    }
}
