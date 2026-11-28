package com.example.demo.service;

import com.example.demo.dto.response.VehicleDocumentExpiryResponse;

import java.util.List;

public interface VehicleDocumentExpiryService {

    int DEFAULT_EXPIRY_WINDOW_DAYS = 30;

    List<VehicleDocumentExpiryResponse> getExpiredDocuments();

    List<VehicleDocumentExpiryResponse> getExpiringSoonDocuments(int days);

    List<VehicleDocumentExpiryResponse> getExpiryAlerts(int days);

    List<VehicleDocumentExpiryResponse> getVehicleExpiryStatus(String vehicleId, int days);
}
