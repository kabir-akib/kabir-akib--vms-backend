package com.example.demo.repository;

import com.example.demo.model.entity.Driver;
import com.example.demo.model.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, String> {

    boolean existsByDrivingLicenceNumberIgnoreCase(String drivingLicenceNumber);

    boolean existsByDrivingLicenceNumberIgnoreCaseAndIdNot(String drivingLicenceNumber, String id);

    List<Driver> findAllByOrderByCreatedAtDesc();

    long countByStatus(DriverStatus status);
}
