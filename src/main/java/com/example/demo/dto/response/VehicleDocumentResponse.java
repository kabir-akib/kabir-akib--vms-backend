package com.example.demo.dto.response;

import com.example.demo.model.entity.VehicleDocument;
import com.example.demo.model.enums.DocumentType;

import java.time.Instant;
import java.time.LocalDate;

public class VehicleDocumentResponse {

    private String id;
    private String vehicleId;
    private String vehicleNumber;
    private DocumentType documentType;
    private String documentTypeLabel;
    private String originalFilename;
    private String storedFileRef;
    private String contentType;
    private long fileSize;
    private Instant uploadDate;
    private LocalDate expiryDate;
    private String downloadUrl;

    public VehicleDocumentResponse() {
    }

    public static VehicleDocumentResponse fromEntity(VehicleDocument document) {
        if (document == null) {
            return null;
        }
        VehicleDocumentResponse response = new VehicleDocumentResponse();
        response.setId(document.getId());
        if (document.getVehicle() != null) {
            response.setVehicleId(document.getVehicle().getId());
            response.setVehicleNumber(document.getVehicle().getVehicleNumber());
            response.setDownloadUrl(String.format(
                    "/api/v1/vehicles/%s/documents/%s/download",
                    document.getVehicle().getId(),
                    document.getId()));
        }
        response.setDocumentType(document.getDocumentType());
        response.setDocumentTypeLabel(
                document.getDocumentType() != null ? document.getDocumentType().getLabel() : null);
        response.setOriginalFilename(document.getOriginalFilename());
        response.setStoredFileRef(document.getStoredFileRef());
        response.setContentType(document.getContentType());
        response.setFileSize(document.getFileSize());
        response.setUploadDate(document.getUploadDate());
        response.setExpiryDate(document.getExpiryDate());
        return response;
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

    public String getStoredFileRef() {
        return storedFileRef;
    }

    public void setStoredFileRef(String storedFileRef) {
        this.storedFileRef = storedFileRef;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public Instant getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Instant uploadDate) {
        this.uploadDate = uploadDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
