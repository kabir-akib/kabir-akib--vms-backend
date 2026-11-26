package com.example.demo.dto.request;

import com.example.demo.model.enums.VehicleStatus;
import com.example.demo.model.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VehicleUpdateRequest {

    @NotBlank(message = "Vehicle number is required")
    @Size(min = 3, max = 32, message = "Vehicle number must be between 3 and 32 characters")
    private String vehicleNumber;

    @NotBlank(message = "Company is required")
    @Size(max = 120, message = "Company cannot exceed 120 characters")
    private String company;

    @NotNull(message = "Vehicle type is required (e.g. TRUCK, VAN, HEAVY_HAULER, SEDAN, SUV, ELECTRIC)")
    private VehicleType type;

    @NotBlank(message = "Brand / Manufacturer is required")
    @Size(max = 80, message = "Brand cannot exceed 80 characters")
    private String brand;

    @NotBlank(message = "Model is required")
    @Size(max = 80, message = "Model cannot exceed 80 characters")
    private String model;

    @NotNull(message = "Vehicle status is required (ACTIVE, MAINTENANCE, INACTIVE)")
    private VehicleStatus status;

    public VehicleUpdateRequest() {
    }

    public VehicleUpdateRequest(String vehicleNumber, String company, VehicleType type,
                                String brand, String model, VehicleStatus status) {
        this.vehicleNumber = vehicleNumber;
        this.company = company;
        this.type = type;
        this.brand = brand;
        this.model = model;
        this.status = status;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }
}

