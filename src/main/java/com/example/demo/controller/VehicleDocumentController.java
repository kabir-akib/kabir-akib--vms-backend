package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.VehicleDocumentResponse;
import com.example.demo.model.enums.DocumentType;
import com.example.demo.service.VehicleDocumentService;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles/{vehicleId}/documents")
public class VehicleDocumentController {

    private final VehicleDocumentService vehicleDocumentService;

    public VehicleDocumentController(VehicleDocumentService vehicleDocumentService) {
        this.vehicleDocumentService = vehicleDocumentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VehicleDocumentResponse>> uploadDocument(
            @PathVariable String vehicleId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam(value = "expiryDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate) {
        VehicleDocumentResponse response = vehicleDocumentService.uploadDocument(
                vehicleId, file, documentType, expiryDate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleDocumentResponse>>> listDocuments(
            @PathVariable String vehicleId) {
        List<VehicleDocumentResponse> documents = vehicleDocumentService.getDocumentsForVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.success(documents));
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable String vehicleId,
            @PathVariable String documentId) {
        VehicleDocumentService.DownloadedDocument downloaded =
                vehicleDocumentService.downloadDocument(vehicleId, documentId);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            if (downloaded.contentType() != null && !downloaded.contentType().isBlank()) {
                mediaType = MediaType.parseMediaType(downloaded.contentType());
            }
        } catch (IllegalArgumentException ignored) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(downloaded.originalFilename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(downloaded.resource());
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable String vehicleId,
            @PathVariable String documentId) {
        vehicleDocumentService.deleteDocument(vehicleId, documentId);
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
    }
}
