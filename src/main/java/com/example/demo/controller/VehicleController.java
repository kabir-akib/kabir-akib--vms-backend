package com.example.demo.controller;

import com.example.demo.dto.request.VehicleCreateRequest;
import com.example.demo.dto.request.VehicleUpdateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.FleetMetricsResponse;
import com.example.demo.dto.response.VehicleResponse;
import com.example.demo.model.enums.VehicleStatus;
import com.example.demo.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> registerVehicle(
            @Valid @RequestBody VehicleCreateRequest request) {
        VehicleResponse created = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle registered successfully", created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAllVehicles(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) String company) {
        List<VehicleResponse> vehicles = vehicleService.getVehicles(query, status, company);
        return ResponseEntity.ok(ApiResponse.success(vehicles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(
            @PathVariable String id) {
        VehicleResponse vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResponse.success(vehicle));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable String id,
            @Valid @RequestBody VehicleUpdateRequest request) {
        VehicleResponse updated = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @PathVariable String id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle deleted successfully", null));
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<FleetMetricsResponse>> getFleetMetrics() {
        FleetMetricsResponse metrics = vehicleService.getFleetMetrics();
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableCompanies() {
        List<String> companies = vehicleService.getAvailableCompanies();
        return ResponseEntity.ok(ApiResponse.success(companies));
    }
}

