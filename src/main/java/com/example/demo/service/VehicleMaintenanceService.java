package com.example.demo.service;

import com.example.demo.dto.request.VehicleMaintenanceRequest;
import com.example.demo.dto.response.VehicleMaintenanceResponse;

import java.util.List;

public interface VehicleMaintenanceService {

    VehicleMaintenanceResponse createMaintenance(String vehicleId, VehicleMaintenanceRequest request);

    List<VehicleMaintenanceResponse> getMaintenanceHistory(String vehicleId);

    VehicleMaintenanceResponse getMaintenanceById(String vehicleId, String maintenanceId);

    VehicleMaintenanceResponse updateMaintenance(String vehicleId, String maintenanceId, VehicleMaintenanceRequest request);

    void deleteMaintenance(String vehicleId, String maintenanceId);
}
