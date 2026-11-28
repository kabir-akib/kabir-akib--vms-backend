package com.example.demo.dto.response;

import com.example.demo.model.entity.Driver;
import com.example.demo.model.entity.DriverVehicleAssignment;
import com.example.demo.model.entity.Vehicle;
import com.example.demo.model.enums.AssignmentStatus;

import java.time.Instant;
import java.time.LocalDate;

public class AssignmentResponse {

    private String id;
    private String driverId;
    private String driverName;
    private String drivingLicenceNumber;
    private String vehicleId;
    private String vehicleNumber;
    private String vehicleDisplayName;
    private LocalDate assignedDate;
    private LocalDate endDate;
    private AssignmentStatus status;
    private String statusLabel;
    private Instant createdAt;
    private Instant updatedAt;

    public static AssignmentResponse fromEntity(DriverVehicleAssignment entity) {
        AssignmentResponse dto = new AssignmentResponse();
        Driver driver = entity.getDriver();
        Vehicle vehicle = entity.getVehicle();

        dto.setId(entity.getId());
        dto.setDriverId(driver.getId());
        dto.setDriverName(driver.getName());
        dto.setDrivingLicenceNumber(driver.getDrivingLicenceNumber());
        dto.setVehicleId(vehicle.getId());
        dto.setVehicleNumber(vehicle.getVehicleNumber());
        dto.setVehicleDisplayName(vehicle.getDisplayName());
        dto.setAssignedDate(entity.getAssignedDate());
        dto.setEndDate(entity.getEndDate());
        dto.setStatus(entity.getStatus());
        dto.setStatusLabel(entity.getStatus().getLabel());
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

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDrivingLicenceNumber() {
        return drivingLicenceNumber;
    }

    public void setDrivingLicenceNumber(String drivingLicenceNumber) {
        this.drivingLicenceNumber = drivingLicenceNumber;
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

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
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
