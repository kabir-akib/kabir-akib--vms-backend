package com.example.demo.controller;

import com.example.demo.dto.request.VehicleMaintenanceRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.VehicleMaintenanceResponse;
import com.example.demo.service.VehicleMaintenanceService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles/{vehicleId}/maintenance")
public class VehicleMaintenanceController {

    private final VehicleMaintenanceService maintenanceService;

    public VehicleMaintenanceController(VehicleMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleMaintenanceResponse>> createMaintenance(
            @PathVariable String vehicleId,
            @Valid @RequestBody VehicleMaintenanceRequest request) {
        VehicleMaintenanceResponse created = maintenanceService.createMaintenance(vehicleId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Maintenance record created successfully", created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleMaintenanceResponse>>> getMaintenanceHistory(
            @PathVariable String vehicleId) {
        List<VehicleMaintenanceResponse> history = maintenanceService.getMaintenanceHistory(vehicleId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/{maintenanceId}")
    public ResponseEntity<ApiResponse<VehicleMaintenanceResponse>> getMaintenanceById(
            @PathVariable String vehicleId,
            @PathVariable String maintenanceId) {
        VehicleMaintenanceResponse response = maintenanceService.getMaintenanceById(vehicleId, maintenanceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{maintenanceId}")
    public ResponseEntity<ApiResponse<VehicleMaintenanceResponse>> updateMaintenance(
            @PathVariable String vehicleId,
            @PathVariable String maintenanceId,
            @Valid @RequestBody VehicleMaintenanceRequest request) {
        VehicleMaintenanceResponse updated = maintenanceService.updateMaintenance(vehicleId, maintenanceId, request);
        return ResponseEntity.ok(ApiResponse.success("Maintenance record updated successfully", updated));
    }

    @DeleteMapping("/{maintenanceId}")
    public ResponseEntity<ApiResponse<Void>> deleteMaintenance(
            @PathVariable String vehicleId,
            @PathVariable String maintenanceId) {
        maintenanceService.deleteMaintenance(vehicleId, maintenanceId);
        return ResponseEntity.ok(ApiResponse.success("Maintenance record deleted successfully", null));
    }
}
