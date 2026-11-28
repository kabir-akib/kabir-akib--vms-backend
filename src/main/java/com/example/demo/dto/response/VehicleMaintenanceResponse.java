package com.example.demo.dto.response;

import com.example.demo.model.entity.Vehicle;
import com.example.demo.model.entity.VehicleMaintenance;
import com.example.demo.model.enums.MaintenanceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class VehicleMaintenanceResponse {

    private String id;
    private String vehicleId;
    private String vehicleNumber;
    private String vehicleDisplayName;
    private LocalDate maintenanceDate;
    private MaintenanceType maintenanceType;
    private String maintenanceTypeLabel;
    private BigDecimal cost;
    private String vendorName;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    public static VehicleMaintenanceResponse fromEntity(VehicleMaintenance entity) {
        VehicleMaintenanceResponse dto = new VehicleMaintenanceResponse();
        Vehicle vehicle = entity.getVehicle();
        dto.setId(entity.getId());
        dto.setVehicleId(vehicle.getId());
        dto.setVehicleNumber(vehicle.getVehicleNumber());
        dto.setVehicleDisplayName(vehicle.getDisplayName());
        dto.setMaintenanceDate(entity.getMaintenanceDate());
        dto.setMaintenanceType(entity.getMaintenanceType());
        dto.setMaintenanceTypeLabel(entity.getMaintenanceType().getLabel());
        dto.setCost(entity.getCost());
        dto.setVendorName(entity.getVendorName());
        dto.setNotes(entity.getNotes());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleDisplayName() {
        return vehicleDisplayName;
    }

    public void setVehicleDisplayName(String vehicleDisplayName) {
        this.vehicleDisplayName = vehicleDisplayName;
    }

    public LocalDate getMaintenanceDate() {
        return maintenanceDate;
    }

    public void setMaintenanceDate(LocalDate maintenanceDate) {
        this.maintenanceDate = maintenanceDate;
    }

    public MaintenanceType getMaintenanceType() {
        return maintenanceType;
    }

    public void setMaintenanceType(MaintenanceType maintenanceType) {
        this.maintenanceType = maintenanceType;
    }

    public String getMaintenanceTypeLabel() {
        return maintenanceTypeLabel;
    }

    public void setMaintenanceTypeLabel(String maintenanceTypeLabel) {
        this.maintenanceTypeLabel = maintenanceTypeLabel;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
