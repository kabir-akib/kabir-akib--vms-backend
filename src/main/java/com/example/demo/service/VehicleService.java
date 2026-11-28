package com.example.demo.service;

import com.example.demo.dto.request.VehicleCreateRequest;
import com.example.demo.dto.request.VehicleUpdateRequest;
import com.example.demo.dto.response.FleetMetricsResponse;
import com.example.demo.dto.response.PagedResponse;
import com.example.demo.dto.response.VehicleResponse;
import com.example.demo.model.enums.VehicleStatus;
import com.example.demo.model.enums.VehicleType;

import java.util.List;

public interface VehicleService {

    VehicleResponse createVehicle(VehicleCreateRequest request);

    VehicleResponse updateVehicle(String id, VehicleUpdateRequest request);

    VehicleResponse updateVehicleStatus(String id, VehicleStatus status);

    VehicleResponse getVehicleById(String id);

    List<VehicleResponse> getVehicles(String query, VehicleStatus status, String company);

    List<VehicleResponse> searchVehicles(
            String query,
            String vehicleNumber,
            String company,
            VehicleType type,
            String brand,
            String model,
            VehicleStatus status
    );

    PagedResponse<VehicleResponse> getVehiclesPaginated(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String query,
            String vehicleNumber,
            String company,
            VehicleType type,
            String brand,
            String model,
            VehicleStatus status
    );

    void deleteVehicle(String id);

    FleetMetricsResponse getFleetMetrics();

    List<String> getAvailableCompanies();
}

