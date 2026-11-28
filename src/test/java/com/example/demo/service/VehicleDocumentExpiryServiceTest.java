package com.example.demo.service;

import com.example.demo.dto.response.VehicleDocumentExpiryResponse;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.Vehicle;
import com.example.demo.model.entity.VehicleDocument;
import com.example.demo.model.enums.DocumentExpiryStatus;
import com.example.demo.model.enums.DocumentType;
import com.example.demo.model.enums.VehicleStatus;
import com.example.demo.model.enums.VehicleType;
import com.example.demo.repository.VehicleDocumentRepository;
import com.example.demo.repository.VehicleRepository;
import com.example.demo.service.impl.VehicleDocumentExpiryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleDocumentExpiryService Unit Tests")
class VehicleDocumentExpiryServiceTest {

    private static final LocalDate FIXED_TODAY = LocalDate.of(2026, 11, 28);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            FIXED_TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);

    @Mock
    private VehicleDocumentRepository vehicleDocumentRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    private VehicleDocumentExpiryServiceImpl expiryService;
    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        expiryService = new VehicleDocumentExpiryServiceImpl(
                vehicleDocumentRepository,
                vehicleRepository,
                FIXED_CLOCK
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
    @DisplayName("classifyExpiryStatus returns EXPIRED for past dates")
    void classifyExpiryStatus_PastDate_ReturnsExpired() {
        assertThat(expiryService.classifyExpiryStatus(FIXED_TODAY.minusDays(1), FIXED_TODAY, 30))
                .isEqualTo(DocumentExpiryStatus.EXPIRED);
    }

    @Test
    @DisplayName("classifyExpiryStatus returns EXPIRING_SOON for today and dates within window")
    void classifyExpiryStatus_WithinWindow_ReturnsExpiringSoon() {
        assertThat(expiryService.classifyExpiryStatus(FIXED_TODAY, FIXED_TODAY, 30))
                .isEqualTo(DocumentExpiryStatus.EXPIRING_SOON);
        assertThat(expiryService.classifyExpiryStatus(FIXED_TODAY.plusDays(30), FIXED_TODAY, 30))
                .isEqualTo(DocumentExpiryStatus.EXPIRING_SOON);
    }

    @Test
    @DisplayName("classifyExpiryStatus returns VALID for dates beyond window")
    void classifyExpiryStatus_BeyondWindow_ReturnsValid() {
        assertThat(expiryService.classifyExpiryStatus(FIXED_TODAY.plusDays(31), FIXED_TODAY, 30))
                .isEqualTo(DocumentExpiryStatus.VALID);
    }

    @Test
    @DisplayName("getExpiredDocuments returns expired documents with vehicle info and excludes null expiry")
    void getExpiredDocuments_ReturnsExpiredAndExcludesNull() {
        VehicleDocument expiredDoc = buildDoc("doc-1", DocumentType.INSURANCE, FIXED_TODAY.minusDays(5));
        VehicleDocument nullExpiryDoc = buildDoc("doc-2", DocumentType.OTHER, null);

        when(vehicleDocumentRepository.findExpiredDocuments(FIXED_TODAY))
                .thenReturn(List.of(expiredDoc, nullExpiryDoc));

        List<VehicleDocumentExpiryResponse> results = expiryService.getExpiredDocuments();

        assertThat(results).hasSize(1);
        VehicleDocumentExpiryResponse res = results.get(0);
        assertThat(res.getDocumentId()).isEqualTo("doc-1");
        assertThat(res.getDocumentType()).isEqualTo(DocumentType.INSURANCE);
        assertThat(res.getVehicleId()).isEqualTo("VMS-01");
        assertThat(res.getVehicleNumber()).isEqualTo("STK-1042");
        assertThat(res.getExpiryStatus()).isEqualTo(DocumentExpiryStatus.EXPIRED);
        assertThat(res.getDaysUntilExpiry()).isEqualTo(-5L);
    }

    @Test
    @DisplayName("getExpiringSoonDocuments returns documents within 30-day window")
    void getExpiringSoonDocuments_ReturnsWithinWindow() {
        VehicleDocument soonDoc = buildDoc("doc-3", DocumentType.TAX_TOKEN, FIXED_TODAY.plusDays(10));

        when(vehicleDocumentRepository.findExpiringSoonDocuments(FIXED_TODAY, FIXED_TODAY.plusDays(30)))
                .thenReturn(List.of(soonDoc));

        List<VehicleDocumentExpiryResponse> results = expiryService.getExpiringSoonDocuments(30);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getExpiryStatus()).isEqualTo(DocumentExpiryStatus.EXPIRING_SOON);
        assertThat(results.get(0).getDaysUntilExpiry()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getExpiryAlerts returns both expired and expiring-soon documents")
    void getExpiryAlerts_ReturnsCombinedAlerts() {
        VehicleDocument expiredDoc = buildDoc("doc-1", DocumentType.INSURANCE, FIXED_TODAY.minusDays(2));
        VehicleDocument soonDoc = buildDoc("doc-2", DocumentType.FITNESS_CERTIFICATE, FIXED_TODAY.plusDays(15));

        when(vehicleDocumentRepository.findExpiryAlerts(FIXED_TODAY.plusDays(30)))
                .thenReturn(List.of(expiredDoc, soonDoc));

        List<VehicleDocumentExpiryResponse> results = expiryService.getExpiryAlerts(30);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getExpiryStatus()).isEqualTo(DocumentExpiryStatus.EXPIRED);
        assertThat(results.get(1).getExpiryStatus()).isEqualTo(DocumentExpiryStatus.EXPIRING_SOON);
    }

    @Test
    @DisplayName("getVehicleExpiryStatus returns expiry status for existing vehicle")
    void getVehicleExpiryStatus_ExistingVehicle_ReturnsStatuses() {
        VehicleDocument validDoc = buildDoc("doc-4", DocumentType.REGISTRATION_CERTIFICATE, FIXED_TODAY.plusDays(90));

        when(vehicleRepository.existsById("VMS-01")).thenReturn(true);
        when(vehicleDocumentRepository.findByVehicleIdWithExpiryDate("VMS-01"))
                .thenReturn(List.of(validDoc));

        List<VehicleDocumentExpiryResponse> results = expiryService.getVehicleExpiryStatus("VMS-01", 30);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getExpiryStatus()).isEqualTo(DocumentExpiryStatus.VALID);
        verify(vehicleRepository).existsById("VMS-01");
    }

    @Test
    @DisplayName("getVehicleExpiryStatus throws ResourceNotFoundException for unknown vehicle")
    void getVehicleExpiryStatus_UnknownVehicle_ThrowsNotFound() {
        when(vehicleRepository.existsById("UNKNOWN")).thenReturn(false);

        assertThatThrownBy(() -> expiryService.getVehicleExpiryStatus("UNKNOWN", 30))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Invalid window days (< 1 or > 365) throws InvalidOperationException")
    void validateWindowDays_OutOfRange_ThrowsInvalidOperation() {
        assertThatThrownBy(() -> expiryService.getExpiringSoonDocuments(0))
                .isInstanceOf(InvalidOperationException.class);
        assertThatThrownBy(() -> expiryService.getExpiryAlerts(366))
                .isInstanceOf(InvalidOperationException.class);
    }

    private VehicleDocument buildDoc(String id, DocumentType type, LocalDate expiryDate) {
        VehicleDocument doc = new VehicleDocument();
        doc.setId(id);
        doc.setVehicle(sampleVehicle);
        doc.setDocumentType(type);
        doc.setOriginalFilename(type.name().toLowerCase() + ".pdf");
        doc.setStoredFileRef("VMS-01/" + id + ".pdf");
        doc.setContentType("application/pdf");
        doc.setFileSize(1024L);
        doc.setUploadDate(Instant.now(FIXED_CLOCK));
        doc.setExpiryDate(expiryDate);
        return doc;
    }
}
