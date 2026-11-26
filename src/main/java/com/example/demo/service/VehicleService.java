package com.example.demo.service;

import com.example.demo.dto.request.VehicleCreateRequest;
import com.example.demo.dto.request.VehicleUpdateRequest;
import com.example.demo.dto.response.FleetMetricsResponse;
import com.example.demo.dto.response.VehicleResponse;
import com.example.demo.model.enums.VehicleStatus;

import java.util.List;

public interface VehicleService {

    VehicleResponse createVehicle(VehicleCreateRequest request);

    VehicleResponse updateVehicle(String id, VehicleUpdateRequest request);

    VehicleResponse getVehicleById(String id);

    List<VehicleResponse> getVehicles(String query, VehicleStatus status, String company);

    void deleteVehicle(String id);

    FleetMetricsResponse getFleetMetrics();

    List<String> getAvailableCompanies();
}

