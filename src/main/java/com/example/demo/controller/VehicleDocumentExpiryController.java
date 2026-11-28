package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.VehicleDocumentExpiryResponse;
import com.example.demo.service.VehicleDocumentExpiryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
public class VehicleDocumentExpiryController {

    private final VehicleDocumentExpiryService expiryService;

    public VehicleDocumentExpiryController(VehicleDocumentExpiryService expiryService) {
        this.expiryService = expiryService;
    }

    @GetMapping("/expired")
    public ResponseEntity<ApiResponse<List<VehicleDocumentExpiryResponse>>> getExpiredDocuments() {
        List<VehicleDocumentExpiryResponse> expired = expiryService.getExpiredDocuments();
        return ResponseEntity.ok(ApiResponse.success(expired));
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<ApiResponse<List<VehicleDocumentExpiryResponse>>> getExpiringSoonDocuments(
            @RequestParam(defaultValue = "30") int days) {
        List<VehicleDocumentExpiryResponse> expiringSoon = expiryService.getExpiringSoonDocuments(days);
        return ResponseEntity.ok(ApiResponse.success(expiringSoon));
    }

    @GetMapping("/expiry-alerts")
    public ResponseEntity<ApiResponse<List<VehicleDocumentExpiryResponse>>> getExpiryAlerts(
            @RequestParam(defaultValue = "30") int days) {
        List<VehicleDocumentExpiryResponse> alerts = expiryService.getExpiryAlerts(days);
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/vehicles/{vehicleId}/expiry")
    public ResponseEntity<ApiResponse<List<VehicleDocumentExpiryResponse>>> getVehicleExpiryStatus(
            @PathVariable String vehicleId,
            @RequestParam(defaultValue = "30") int days) {
        List<VehicleDocumentExpiryResponse> documents = expiryService.getVehicleExpiryStatus(vehicleId, days);
        return ResponseEntity.ok(ApiResponse.success(documents));
    }
}
