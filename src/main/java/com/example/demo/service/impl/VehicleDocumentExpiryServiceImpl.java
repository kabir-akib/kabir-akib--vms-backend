package com.example.demo.service.impl;

import com.example.demo.dto.response.VehicleDocumentExpiryResponse;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.VehicleDocument;
import com.example.demo.model.enums.DocumentExpiryStatus;
import com.example.demo.repository.VehicleDocumentRepository;
import com.example.demo.repository.VehicleRepository;
import com.example.demo.service.VehicleDocumentExpiryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class VehicleDocumentExpiryServiceImpl implements VehicleDocumentExpiryService {

    private final VehicleDocumentRepository vehicleDocumentRepository;
    private final VehicleRepository vehicleRepository;
    private final Clock clock;

    @Autowired
    public VehicleDocumentExpiryServiceImpl(VehicleDocumentRepository vehicleDocumentRepository,
                                            VehicleRepository vehicleRepository) {
        this(vehicleDocumentRepository, vehicleRepository, Clock.systemDefaultZone());
    }

    public VehicleDocumentExpiryServiceImpl(VehicleDocumentRepository vehicleDocumentRepository,
                                            VehicleRepository vehicleRepository,
                                            Clock clock) {
        this.vehicleDocumentRepository = vehicleDocumentRepository;
        this.vehicleRepository = vehicleRepository;
        this.clock = clock;
    }

    @Override
    public List<VehicleDocumentExpiryResponse> getExpiredDocuments() {
        LocalDate today = LocalDate.now(clock);
        return vehicleDocumentRepository.findExpiredDocuments(today)
                .stream()
                .filter(doc -> doc.getExpiryDate() != null)
                .map(doc -> toResponse(doc, today, DEFAULT_EXPIRY_WINDOW_DAYS))
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleDocumentExpiryResponse> getExpiringSoonDocuments(int days) {
        validateWindowDays(days);
        LocalDate today = LocalDate.now(clock);
        LocalDate endDate = today.plusDays(days);
        return vehicleDocumentRepository.findExpiringSoonDocuments(today, endDate)
                .stream()
                .filter(doc -> doc.getExpiryDate() != null)
                .map(doc -> toResponse(doc, today, days))
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleDocumentExpiryResponse> getExpiryAlerts(int days) {
        validateWindowDays(days);
        LocalDate today = LocalDate.now(clock);
        LocalDate endDate = today.plusDays(days);
        return vehicleDocumentRepository.findExpiryAlerts(endDate)
                .stream()
                .filter(doc -> doc.getExpiryDate() != null)
                .map(doc -> toResponse(doc, today, days))
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleDocumentExpiryResponse> getVehicleExpiryStatus(String vehicleId, int days) {
        validateWindowDays(days);
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle", "id", vehicleId);
        }
        LocalDate today = LocalDate.now(clock);
        return vehicleDocumentRepository.findByVehicleIdWithExpiryDate(vehicleId)
                .stream()
                .filter(doc -> doc.getExpiryDate() != null)
                .map(doc -> toResponse(doc, today, days))
                .collect(Collectors.toList());
    }

    public DocumentExpiryStatus classifyExpiryStatus(LocalDate expiryDate, LocalDate referenceDate, int windowDays) {
        if (expiryDate.isBefore(referenceDate)) {
            return DocumentExpiryStatus.EXPIRED;
        }
        if (!expiryDate.isAfter(referenceDate.plusDays(windowDays))) {
            return DocumentExpiryStatus.EXPIRING_SOON;
        }
        return DocumentExpiryStatus.VALID;
    }

    private VehicleDocumentExpiryResponse toResponse(VehicleDocument doc, LocalDate today, int windowDays) {
        LocalDate expiryDate = doc.getExpiryDate();
        DocumentExpiryStatus status = classifyExpiryStatus(expiryDate, today, windowDays);
        long daysUntilExpiry = ChronoUnit.DAYS.between(today, expiryDate);
        return VehicleDocumentExpiryResponse.fromEntity(doc, status, daysUntilExpiry);
    }

    private void validateWindowDays(int days) {
        if (days < 1 || days > 365) {
            throw new InvalidOperationException("Expiry window days must be between 1 and 365.");
        }
    }
}
