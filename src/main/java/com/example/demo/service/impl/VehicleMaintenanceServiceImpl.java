package com.example.demo.service.impl;

import com.example.demo.dto.request.VehicleMaintenanceRequest;
import com.example.demo.dto.response.VehicleMaintenanceResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.Vehicle;
import com.example.demo.model.entity.VehicleMaintenance;
import com.example.demo.repository.VehicleMaintenanceRepository;
import com.example.demo.repository.VehicleRepository;
import com.example.demo.service.VehicleMaintenanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VehicleMaintenanceServiceImpl implements VehicleMaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(VehicleMaintenanceServiceImpl.class);

    private final VehicleMaintenanceRepository maintenanceRepository;
    private final VehicleRepository vehicleRepository;

    public VehicleMaintenanceServiceImpl(VehicleMaintenanceRepository maintenanceRepository,
                                         VehicleRepository vehicleRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public VehicleMaintenanceResponse createMaintenance(String vehicleId, VehicleMaintenanceRequest request) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        VehicleMaintenance maintenance = new VehicleMaintenance();
        maintenance.setVehicle(vehicle);
        applyRequest(maintenance, request);

        VehicleMaintenance saved = maintenanceRepository.save(maintenance);
        log.info("Created maintenance record id={} vehicleId={} type={} date={}",
                saved.getId(), vehicleId, saved.getMaintenanceType(), saved.getMaintenanceDate());
        return VehicleMaintenanceResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleMaintenanceResponse> getMaintenanceHistory(String vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle", "id", vehicleId);
        }
        return maintenanceRepository.findByVehicleIdOrderByNewestFirst(vehicleId)
                .stream()
                .map(VehicleMaintenanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleMaintenanceResponse getMaintenanceById(String vehicleId, String maintenanceId) {
        VehicleMaintenance maintenance = findByIdAndVehicle(vehicleId, maintenanceId);
        return VehicleMaintenanceResponse.fromEntity(maintenance);
    }

    @Override
    public VehicleMaintenanceResponse updateMaintenance(String vehicleId,
                                                        String maintenanceId,
                                                        VehicleMaintenanceRequest request) {
        VehicleMaintenance maintenance = findByIdAndVehicle(vehicleId, maintenanceId);
        applyRequest(maintenance, request);

        VehicleMaintenance updated = maintenanceRepository.save(maintenance);
        log.info("Updated maintenance record id={} vehicleId={}", updated.getId(), vehicleId);
        return VehicleMaintenanceResponse.fromEntity(updated);
    }

    @Override
    public void deleteMaintenance(String vehicleId, String maintenanceId) {
        VehicleMaintenance maintenance = findByIdAndVehicle(vehicleId, maintenanceId);
        maintenanceRepository.delete(maintenance);
        log.info("Deleted maintenance record id={} vehicleId={}", maintenanceId, vehicleId);
    }

    private VehicleMaintenance findByIdAndVehicle(String vehicleId, String maintenanceId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle", "id", vehicleId);
        }
        return maintenanceRepository.findByIdAndVehicleId(maintenanceId, vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance record", "id", maintenanceId));
    }

    private void applyRequest(VehicleMaintenance maintenance, VehicleMaintenanceRequest request) {
        maintenance.setMaintenanceDate(request.getMaintenanceDate());
        maintenance.setMaintenanceType(request.getMaintenanceType());
        maintenance.setCost(request.getCost());
        maintenance.setVendorName(request.getVendorName().trim());
        maintenance.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);
    }
}
