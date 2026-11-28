package com.example.demo.service.impl;

import com.example.demo.dto.request.VehicleCreateRequest;
import com.example.demo.dto.request.VehicleUpdateRequest;
import com.example.demo.dto.response.FleetMetricsResponse;
import com.example.demo.dto.response.PagedResponse;
import com.example.demo.dto.response.VehicleResponse;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.DuplicateVehicleNumberException;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.Vehicle;
import com.example.demo.model.enums.VehicleStatus;
import com.example.demo.model.enums.VehicleType;
import com.example.demo.repository.VehicleRepository;
import com.example.demo.service.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleServiceImpl.class);

    private final VehicleRepository vehicleRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public VehicleResponse createVehicle(VehicleCreateRequest request) {
        String normalizedNumber = normalizeVehicleNumber(request.getVehicleNumber());

        if (vehicleRepository.existsByVehicleNumberIgnoreCase(normalizedNumber)) {
            throw new DuplicateVehicleNumberException(normalizedNumber);
        }

        Vehicle vehicle = new Vehicle();
        String id = request.getId();
        if (id != null && !id.trim().isEmpty()) {
            id = id.trim();
            if (vehicleRepository.existsById(id)) {
                throw new DuplicateResourceException(
                        String.format("Vehicle with ID '%s' already exists.", id));
            }
            vehicle.setId(id);
        }

        vehicle.setVehicleNumber(normalizedNumber);
        vehicle.setCompany(request.getCompany().trim());
        vehicle.setType(request.getType());
        vehicle.setBrand(request.getBrand().trim());
        vehicle.setModel(request.getModel().trim());
        vehicle.setStatus(request.getStatus() != null ? request.getStatus() : VehicleStatus.ACTIVE);

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Registered new vehicle id={} number={} company={}", saved.getId(), saved.getVehicleNumber(), saved.getCompany());
        return VehicleResponse.fromEntity(saved);
    }

    @Override
    public VehicleResponse updateVehicle(String id, VehicleUpdateRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));

        String normalizedNumber = normalizeVehicleNumber(request.getVehicleNumber());
        if (vehicleRepository.existsByVehicleNumberIgnoreCaseAndIdNot(normalizedNumber, id)) {
            throw new DuplicateVehicleNumberException(normalizedNumber);
        }

        vehicle.setVehicleNumber(normalizedNumber);
        vehicle.setCompany(request.getCompany().trim());
        vehicle.setType(request.getType());
        vehicle.setBrand(request.getBrand().trim());
        vehicle.setModel(request.getModel().trim());
        vehicle.setStatus(request.getStatus());

        Vehicle updated = vehicleRepository.save(vehicle);
        log.info("Updated vehicle id={} number={} status={}", updated.getId(), updated.getVehicleNumber(), updated.getStatus());
        return VehicleResponse.fromEntity(updated);
    }

    @Override
    public VehicleResponse updateVehicleStatus(String id, VehicleStatus status) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));

        vehicle.setStatus(status);
        Vehicle updated = vehicleRepository.save(vehicle);
        log.info("Updated vehicle status id={} number={} status={}", updated.getId(), updated.getVehicleNumber(), updated.getStatus());
        return VehicleResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(String id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));
        return VehicleResponse.fromEntity(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehicles(String query, VehicleStatus status, String company) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        String cleanCompany = (company != null && !company.trim().isEmpty()) ? company.trim() : null;

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<Vehicle> vehicles = vehicleRepository.searchVehicles(cleanQuery, status, cleanCompany, sort);

        return vehicles.stream()
                .map(VehicleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VehicleResponse> getVehiclesPaginated(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String query,
            String vehicleNumber,
            String company,
            VehicleType type,
            String brand,
            String model,
            VehicleStatus status) {
        if (page < 0) {
            throw new InvalidOperationException("Page index must not be negative.");
        }
        if (size < 1 || size > 100) {
            throw new InvalidOperationException("Page size must be between 1 and 100.");
        }

        String resolvedSortBy = (sortBy == null || sortBy.trim().isEmpty()) ? "createdAt" : sortBy.trim();
        if (!java.util.Set.of("vehicleNumber", "company", "type", "brand", "model", "status", "createdAt")
                .contains(resolvedSortBy)) {
            throw new InvalidOperationException("Unsupported sort field: " + resolvedSortBy);
        }

        String resolvedSortDir = (sortDir == null || sortDir.trim().isEmpty()) ? "desc" : sortDir.trim().toLowerCase();
        Sort.Direction direction;
        if ("asc".equals(resolvedSortDir)) {
            direction = Sort.Direction.ASC;
        } else if ("desc".equals(resolvedSortDir)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new InvalidOperationException("Invalid sort direction: " + sortDir + ". Use 'asc' or 'desc'.");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, resolvedSortBy));
        Page<VehicleResponse> mappedPage = vehicleRepository.findFilteredVehicles(
                trimToNull(query),
                trimToNull(vehicleNumber),
                trimToNull(company),
                type,
                trimToNull(brand),
                trimToNull(model),
                status,
                pageable
        ).map(VehicleResponse::fromEntity);

        return PagedResponse.fromPage(mappedPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> searchVehicles(
            String query,
            String vehicleNumber,
            String company,
            VehicleType type,
            String brand,
            String model,
            VehicleStatus status) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<Vehicle> vehicles = vehicleRepository.advancedSearchVehicles(
                trimToNull(query),
                trimToNull(vehicleNumber),
                trimToNull(company),
                type,
                trimToNull(brand),
                trimToNull(model),
                status,
                sort
        );

        return vehicles.stream()
                .map(VehicleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public void deleteVehicle(String id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));
        vehicleRepository.delete(vehicle);
        log.info("Deleted vehicle id={} number={}", id, vehicle.getVehicleNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public FleetMetricsResponse getFleetMetrics() {
        long total = vehicleRepository.count();
        long active = vehicleRepository.countByStatus(VehicleStatus.ACTIVE);
        long maintenance = vehicleRepository.countByStatus(VehicleStatus.MAINTENANCE);
        long inactive = vehicleRepository.countByStatus(VehicleStatus.INACTIVE);

        return new FleetMetricsResponse(total, active, maintenance, inactive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableCompanies() {
        return vehicleRepository.findDistinctCompanies();
    }

    private String normalizeVehicleNumber(String vehicleNumber) {
        if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle number cannot be null or blank");
        }
        return vehicleNumber.trim().toUpperCase();
    }
}

