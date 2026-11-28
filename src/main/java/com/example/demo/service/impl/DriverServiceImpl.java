package com.example.demo.service.impl;

import com.example.demo.dto.request.DriverRequest;
import com.example.demo.dto.response.DriverResponse;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.Driver;
import com.example.demo.repository.DriverRepository;
import com.example.demo.service.DriverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DriverServiceImpl implements DriverService {

    private static final Logger log = LoggerFactory.getLogger(DriverServiceImpl.class);

    private final DriverRepository driverRepository;

    public DriverServiceImpl(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Override
    public DriverResponse createDriver(DriverRequest request) {
        String licence = request.getDrivingLicenceNumber().trim();
        if (driverRepository.existsByDrivingLicenceNumberIgnoreCase(licence)) {
            throw new InvalidOperationException("Driving licence number '" + licence + "' already exists.");
        }

        Driver driver = new Driver();
        applyRequest(driver, request, licence);

        Driver saved = driverRepository.save(driver);
        log.info("Created driver id={} licence={}", saved.getId(), saved.getDrivingLicenceNumber());
        return DriverResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(DriverResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponse getDriverById(String id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", id));
        return DriverResponse.fromEntity(driver);
    }

    @Override
    public DriverResponse updateDriver(String id, DriverRequest request) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", id));

        String licence = request.getDrivingLicenceNumber().trim();
        if (driverRepository.existsByDrivingLicenceNumberIgnoreCaseAndIdNot(licence, id)) {
            throw new InvalidOperationException("Driving licence number '" + licence + "' already exists.");
        }

        applyRequest(driver, request, licence);
        Driver updated = driverRepository.save(driver);
        log.info("Updated driver id={} licence={}", updated.getId(), updated.getDrivingLicenceNumber());
        return DriverResponse.fromEntity(updated);
    }

    @Override
    public void deleteDriver(String id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", id));
        driverRepository.delete(driver);
        log.info("Deleted driver id={} licence={}", id, driver.getDrivingLicenceNumber());
    }

    private void applyRequest(Driver driver, DriverRequest request, String normalizedLicence) {
        driver.setName(request.getName().trim());
        driver.setPhoneNumber(request.getPhoneNumber().trim());
        driver.setDrivingLicenceNumber(normalizedLicence);
        driver.setLicenceExpiryDate(request.getLicenceExpiryDate());
        driver.setAddress(request.getAddress().trim());
        driver.setStatus(request.getStatus());
    }
}
