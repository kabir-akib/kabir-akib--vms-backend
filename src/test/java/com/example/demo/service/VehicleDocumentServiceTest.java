package com.example.demo.service;

import com.example.demo.config.FileStorageProperties;
import com.example.demo.dto.response.VehicleDocumentResponse;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.Vehicle;
import com.example.demo.model.entity.VehicleDocument;
import com.example.demo.model.enums.DocumentType;
import com.example.demo.model.enums.VehicleStatus;
import com.example.demo.model.enums.VehicleType;
import com.example.demo.repository.VehicleDocumentRepository;
import com.example.demo.repository.VehicleRepository;
import com.example.demo.service.impl.VehicleDocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleDocumentService Unit Tests")
class VehicleDocumentServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleDocumentRepository vehicleDocumentRepository;

    @Mock
    private FileStorageService fileStorageService;

    private FileStorageProperties properties;
    private VehicleDocumentServiceImpl documentService;
    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        properties = new FileStorageProperties();
        properties.setMaxFileSizeBytes(1024 * 1024); // 1 MB for tests
        documentService = new VehicleDocumentServiceImpl(
                vehicleRepository,
                vehicleDocumentRepository,
                fileStorageService,
                properties
        );

        sampleVehicle = new Vehicle(
                "VMS-01",
                "STK-1042",
                "Stark Logistics",
                VehicleType.TRUCK,
                "Scania",
                "R500",
                VehicleStatus.ACTIVE
        );
    }

    @Test
    @DisplayName("Should upload valid PDF document with expiry date")
    void uploadDocument_Success_WithExpiryDate() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "insurance-2026.pdf",
                "application/pdf",
                "PDF-BYTES".getBytes()
        );
        LocalDate expiry = LocalDate.of(2027, 12, 31);

        when(vehicleRepository.findById("VMS-01")).thenReturn(Optional.of(sampleVehicle));
        when(fileStorageService.store(eq("VMS-01"), any(MultipartFile.class)))
                .thenReturn("VMS-01/uuid-insurance-2026.pdf");
        when(vehicleDocumentRepository.save(any(VehicleDocument.class))).thenAnswer(invocation -> {
            VehicleDocument doc = invocation.getArgument(0);
            doc.setId("doc-1");
            doc.setUploadDate(Instant.now());
            return doc;
        });

        VehicleDocumentResponse response = documentService.uploadDocument(
                "VMS-01", file, DocumentType.INSURANCE, expiry);

        assertThat(response.getId()).isEqualTo("doc-1");
        assertThat(response.getVehicleId()).isEqualTo("VMS-01");
        assertThat(response.getDocumentType()).isEqualTo(DocumentType.INSURANCE);
        assertThat(response.getDocumentTypeLabel()).isEqualTo("Insurance");
        assertThat(response.getOriginalFilename()).isEqualTo("insurance-2026.pdf");
        assertThat(response.getStoredFileRef()).isEqualTo("VMS-01/uuid-insurance-2026.pdf");
        assertThat(response.getExpiryDate()).isEqualTo(expiry);
        assertThat(response.getDownloadUrl()).isEqualTo("/api/v1/vehicles/VMS-01/documents/doc-1/download");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when uploading for unknown vehicle")
    void uploadDocument_VehicleNotFound_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "data".getBytes());

        when(vehicleRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.uploadDocument(
                "UNKNOWN", file, DocumentType.TAX_TOKEN, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("UNKNOWN");

        verify(fileStorageService, never()).store(any(), any(MultipartFile.class));
        verify(vehicleDocumentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject empty file upload")
    void uploadDocument_EmptyFile_ThrowsException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        when(vehicleRepository.findById("VMS-01")).thenReturn(Optional.of(sampleVehicle));

        assertThatThrownBy(() -> documentService.uploadDocument(
                "VMS-01", emptyFile, DocumentType.REGISTRATION_CERTIFICATE, null))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("empty");

        verify(fileStorageService, never()).store(any(), any(MultipartFile.class));
    }

    @Test
    @DisplayName("Should reject unsupported file extension and content type")
    void uploadDocument_UnsupportedFileType_ThrowsException() {
        MockMultipartFile scriptFile = new MockMultipartFile(
                "file", "payload.sh", "text/plain", "#!/bin/sh".getBytes());

        when(vehicleRepository.findById("VMS-01")).thenReturn(Optional.of(sampleVehicle));

        assertThatThrownBy(() -> documentService.uploadDocument(
                "VMS-01", scriptFile, DocumentType.OTHER, null))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Unsupported file extension");

        verify(fileStorageService, never()).store(any(), any(MultipartFile.class));
    }

    @Test
    @DisplayName("Should reject file exceeding configured max size")
    void uploadDocument_ExceedsMaxSize_ThrowsException() {
        properties.setMaxFileSizeBytes(10);
        MockMultipartFile oversizedFile = new MockMultipartFile(
                "file", "large.png", "image/png", new byte[25]);

        when(vehicleRepository.findById("VMS-01")).thenReturn(Optional.of(sampleVehicle));

        assertThatThrownBy(() -> documentService.uploadDocument(
                "VMS-01", oversizedFile, DocumentType.FITNESS_CERTIFICATE, null))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("maximum allowed limit");

        verify(fileStorageService, never()).store(any(), any(MultipartFile.class));
    }

    @Test
    @DisplayName("Should list documents for an existing vehicle")
    void getDocumentsForVehicle_Success() {
        VehicleDocument doc = new VehicleDocument();
        doc.setId("doc-1");
        doc.setVehicle(sampleVehicle);
        doc.setDocumentType(DocumentType.REGISTRATION_CERTIFICATE);
        doc.setOriginalFilename("reg.pdf");
        doc.setStoredFileRef("VMS-01/reg.pdf");
        doc.setContentType("application/pdf");
        doc.setFileSize(100);
        doc.setUploadDate(Instant.now());

        when(vehicleRepository.existsById("VMS-01")).thenReturn(true);
        when(vehicleDocumentRepository.findByVehicleIdOrderByUploadDateDesc("VMS-01"))
                .thenReturn(List.of(doc));

        List<VehicleDocumentResponse> results = documentService.getDocumentsForVehicle("VMS-01");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDocumentType()).isEqualTo(DocumentType.REGISTRATION_CERTIFICATE);
    }

    @Test
    @DisplayName("Should load resource when downloading a document")
    void downloadDocument_Success() {
        VehicleDocument doc = new VehicleDocument();
        doc.setId("doc-1");
        doc.setVehicle(sampleVehicle);
        doc.setOriginalFilename("reg.pdf");
        doc.setStoredFileRef("VMS-01/reg.pdf");
        doc.setContentType("application/pdf");

        ByteArrayResource resource = new ByteArrayResource("hello".getBytes());

        when(vehicleRepository.existsById("VMS-01")).thenReturn(true);
        when(vehicleDocumentRepository.findByIdAndVehicleId("doc-1", "VMS-01"))
                .thenReturn(Optional.of(doc));
        when(fileStorageService.loadAsResource("VMS-01/reg.pdf")).thenReturn(resource);

        VehicleDocumentService.DownloadedDocument downloaded =
                documentService.downloadDocument("VMS-01", "doc-1");

        assertThat(downloaded.originalFilename()).isEqualTo("reg.pdf");
        assertThat(downloaded.contentType()).isEqualTo("application/pdf");
        assertThat(downloaded.resource()).isSameAs(resource);
    }

    @Test
    @DisplayName("Should delete both DB metadata and stored file")
    void deleteDocument_Success() {
        VehicleDocument doc = new VehicleDocument();
        doc.setId("doc-1");
        doc.setVehicle(sampleVehicle);
        doc.setStoredFileRef("VMS-01/reg.pdf");

        when(vehicleRepository.existsById("VMS-01")).thenReturn(true);
        when(vehicleDocumentRepository.findByIdAndVehicleId("doc-1", "VMS-01"))
                .thenReturn(Optional.of(doc));

        documentService.deleteDocument("VMS-01", "doc-1");

        verify(vehicleDocumentRepository).delete(doc);
        verify(fileStorageService).delete("VMS-01/reg.pdf");
    }
}
