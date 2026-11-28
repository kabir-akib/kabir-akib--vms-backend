package com.example.demo.service;

import com.example.demo.dto.response.VehicleDocumentResponse;
import com.example.demo.model.enums.DocumentType;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface VehicleDocumentService {

    VehicleDocumentResponse uploadDocument(
            String vehicleId,
            MultipartFile file,
            DocumentType documentType,
            LocalDate expiryDate
    );

    List<VehicleDocumentResponse> getDocumentsForVehicle(String vehicleId);

    DownloadedDocument downloadDocument(String vehicleId, String documentId);

    void deleteDocument(String vehicleId, String documentId);

    record DownloadedDocument(Resource resource, String contentType, String originalFilename) {
    }
}
