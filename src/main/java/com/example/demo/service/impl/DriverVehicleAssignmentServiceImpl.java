package com.example.demo.service.impl;

import com.example.demo.dto.request.AssignmentRequest;
import com.example.demo.dto.response.AssignmentResponse;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.Driver;
import com.example.demo.model.entity.DriverVehicleAssignment;
import com.example.demo.model.entity.Vehicle;
import com.example.demo.model.enums.AssignmentStatus;
import com.example.demo.repository.DriverRepository;
import com.example.demo.repository.DriverVehicleAssignmentRepository;
import com.example.demo.repository.VehicleRepository;
import com.example.demo.service.DriverVehicleAssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DriverVehicleAssignmentServiceImpl implements DriverVehicleAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(DriverVehicleAssignmentServiceImpl.class);

    private final DriverVehicleAssignmentRepository assignmentRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    public DriverVehicleAssignmentServiceImpl(DriverVehicleAssignmentRepository assignmentRepository,
                                              DriverRepository driverRepository,
                                              VehicleRepository vehicleRepository) {
        this.assignmentRepository = assignmentRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public AssignmentResponse createAssignment(AssignmentRequest request) {
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", request.getDriverId()));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", request.getVehicleId()));

        if (assignmentRepository.existsByDriverIdAndStatus(driver.getId(), AssignmentStatus.ACTIVE)) {
            throw new InvalidOperationException("Driver already has an active vehicle assignment.");
        }

        if (assignmentRepository.existsByVehicleIdAndStatus(vehicle.getId(), AssignmentStatus.ACTIVE)) {
            throw new InvalidOperationException("Vehicle already has an active driver assignment.");
        }

        DriverVehicleAssignment assignment = new DriverVehicleAssignment();
        assignment.setDriver(driver);
        assignment.setVehicle(vehicle);
        assignment.setAssignedDate(request.getAssignedDate());
        assignment.setStatus(AssignmentStatus.ACTIVE);

        DriverVehicleAssignment saved = assignmentRepository.save(assignment);
        log.info("Created assignment id={} driverId={} vehicleId={}", saved.getId(), driver.getId(), vehicle.getId());
        return AssignmentResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAllAssignments() {
        return assignmentRepository.findAllWithDetails()
                .stream()
                .map(AssignmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentById(String id) {
        DriverVehicleAssignment assignment = assignmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", id));
        return AssignmentResponse.fromEntity(assignment);
    }

    @Override
    public AssignmentResponse endAssignment(String id) {
        DriverVehicleAssignment assignment = assignmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", id));

        if (assignment.getStatus() == AssignmentStatus.INACTIVE) {
            throw new InvalidOperationException("Assignment is already ended.");
        }

        assignment.setStatus(AssignmentStatus.INACTIVE);
        assignment.setEndDate(LocalDate.now());

        DriverVehicleAssignment updated = assignmentRepository.save(assignment);
        log.info("Ended assignment id={}", id);
        return AssignmentResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsForVehicle(String vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle", "id", vehicleId);
        }
        return assignmentRepository.findByVehicleIdWithDetails(vehicleId)
                .stream()
                .map(AssignmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsForDriver(String driverId) {
        if (!driverRepository.existsById(driverId)) {
            throw new ResourceNotFoundException("Driver", "id", driverId);
        }
        return assignmentRepository.findByDriverIdWithDetails(driverId)
                .stream()
                .map(AssignmentResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
