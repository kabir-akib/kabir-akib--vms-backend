package com.example.demo.dto.response;

import com.example.demo.model.entity.Vehicle;
import com.example.demo.model.enums.VehicleStatus;
import com.example.demo.model.enums.VehicleType;

import java.time.Instant;

public class VehicleResponse {

    private String id;
    private String vehicleNumber;
    private String company;
    private VehicleType type;
    private String typeLabel;
    private String brand;
    private String model;
    private String displayName;
    private VehicleStatus status;
    private String statusLabel;
    private Instant createdAt;
    private Instant updatedAt;

    public VehicleResponse() {
    }

    public static VehicleResponse fromEntity(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        VehicleResponse response = new VehicleResponse();
        response.setId(vehicle.getId());
        response.setVehicleNumber(vehicle.getVehicleNumber());
        response.setCompany(vehicle.getCompany());
        response.setType(vehicle.getType());
        response.setTypeLabel(vehicle.getType() != null ? vehicle.getType().getLabel() : null);
        response.setBrand(vehicle.getBrand());
        response.setModel(vehicle.getModel());
        response.setDisplayName(vehicle.getDisplayName());
        response.setStatus(vehicle.getStatus());
        response.setStatusLabel(vehicle.getStatus() != null ? vehicle.getStatus().getLabel() : null);
        response.setCreatedAt(vehicle.getCreatedAt());
        response.setUpdatedAt(vehicle.getUpdatedAt());
        return response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTypeLabel() {
        return typeLabel;
    }

    public void setTypeLabel(String typeLabel) {
        this.typeLabel = typeLabel;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

