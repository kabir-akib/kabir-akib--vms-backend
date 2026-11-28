package com.example.demo.dto.response;

import com.example.demo.model.entity.Vehicle;
import com.example.demo.model.entity.VehicleDocument;
import com.example.demo.model.enums.DocumentExpiryStatus;
import com.example.demo.model.enums.DocumentType;

import java.time.LocalDate;

public class VehicleDocumentExpiryResponse {

    private String documentId;
    private DocumentType documentType;
    private String documentTypeLabel;
    private String originalFilename;

    private String vehicleId;
    private String vehicleNumber;
    private String company;
    private String brand;
    private String model;
    private String vehicleDisplayName;

    private LocalDate expiryDate;
    private long daysUntilExpiry;
    private DocumentExpiryStatus expiryStatus;
    private String expiryStatusLabel;

    public VehicleDocumentExpiryResponse() {
    }

    public static VehicleDocumentExpiryResponse fromEntity(
            VehicleDocument document,
            DocumentExpiryStatus expiryStatus,
            long daysUntilExpiry) {
        if (document == null) {
            return null;
        }
        VehicleDocumentExpiryResponse response = new VehicleDocumentExpiryResponse();
        response.setDocumentId(document.getId());
        response.setDocumentType(document.getDocumentType());
        response.setDocumentTypeLabel(
                document.getDocumentType() != null ? document.getDocumentType().getLabel() : null);
        response.setOriginalFilename(document.getOriginalFilename());

        Vehicle vehicle = document.getVehicle();
        if (vehicle != null) {
            response.setVehicleId(vehicle.getId());
            response.setVehicleNumber(vehicle.getVehicleNumber());
            response.setCompany(vehicle.getCompany());
            response.setBrand(vehicle.getBrand());
            response.setModel(vehicle.getModel());
            response.setVehicleDisplayName(vehicle.getDisplayName());
        }

        response.setExpiryDate(document.getExpiryDate());
        response.setDaysUntilExpiry(daysUntilExpiry);
        response.setExpiryStatus(expiryStatus);
        response.setExpiryStatusLabel(expiryStatus != null ? expiryStatus.getLabel() : null);
        return response;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getDocumentTypeLabel() {
        return documentTypeLabel;
    }

    public void setDocumentTypeLabel(String documentTypeLabel) {
        this.documentTypeLabel = documentTypeLabel;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
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

    public String getVehicleDisplayName() {
        return vehicleDisplayName;
    }

    public void setVehicleDisplayName(String vehicleDisplayName) {
        this.vehicleDisplayName = vehicleDisplayName;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public long getDaysUntilExpiry() {
        return daysUntilExpiry;
    }

    public void setDaysUntilExpiry(long daysUntilExpiry) {
        this.daysUntilExpiry = daysUntilExpiry;
    }

    public DocumentExpiryStatus getExpiryStatus() {
        return expiryStatus;
    }

    public void setExpiryStatus(DocumentExpiryStatus expiryStatus) {
        this.expiryStatus = expiryStatus;
    }

    public String getExpiryStatusLabel() {
        return expiryStatusLabel;
    }

    public void setExpiryStatusLabel(String expiryStatusLabel) {
        this.expiryStatusLabel = expiryStatusLabel;
    }
}
