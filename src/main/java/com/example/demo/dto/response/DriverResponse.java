package com.example.demo.dto.response;

import com.example.demo.model.entity.Driver;
import com.example.demo.model.enums.DriverStatus;

import java.time.Instant;
import java.time.LocalDate;

public class DriverResponse {

    private String id;
    private String name;
    private String phoneNumber;
    private String drivingLicenceNumber;
    private LocalDate licenceExpiryDate;
    private String address;
    private DriverStatus status;
    private String statusLabel;
    private Instant createdAt;
    private Instant updatedAt;

    public static DriverResponse fromEntity(Driver driver) {
        DriverResponse dto = new DriverResponse();
        dto.setId(driver.getId());
        dto.setName(driver.getName());
        dto.setPhoneNumber(driver.getPhoneNumber());
        dto.setDrivingLicenceNumber(driver.getDrivingLicenceNumber());
        dto.setLicenceExpiryDate(driver.getLicenceExpiryDate());
        dto.setAddress(driver.getAddress());
        dto.setStatus(driver.getStatus());
        dto.setStatusLabel(driver.getStatus().getLabel());
        dto.setCreatedAt(driver.getCreatedAt());
        dto.setUpdatedAt(driver.getUpdatedAt());
        return dto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDrivingLicenceNumber() {
        return drivingLicenceNumber;
    }

    public void setDrivingLicenceNumber(String drivingLicenceNumber) {
        this.drivingLicenceNumber = drivingLicenceNumber;
    }

    public LocalDate getLicenceExpiryDate() {
        return licenceExpiryDate;
    }

    public void setLicenceExpiryDate(LocalDate licenceExpiryDate) {
        this.licenceExpiryDate = licenceExpiryDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public DriverStatus getStatus() {
        return status;
    }

    public void setStatus(DriverStatus status) {
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
