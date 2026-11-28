package com.example.demo.repository;

import com.example.demo.model.entity.Vehicle;
import com.example.demo.model.enums.VehicleStatus;
import com.example.demo.model.enums.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    boolean existsByVehicleNumberIgnoreCase(String vehicleNumber);

    boolean existsByVehicleNumberIgnoreCaseAndIdNot(String vehicleNumber, String id);

    Optional<Vehicle> findByVehicleNumberIgnoreCase(String vehicleNumber);

    long countByStatus(VehicleStatus status);

    @Query("SELECT DISTINCT v.company FROM Vehicle v WHERE v.company IS NOT NULL ORDER BY v.company ASC")
    List<String> findDistinctCompanies();

    @Query("SELECT v FROM Vehicle v WHERE " +
           "(:status IS NULL OR v.status = :status) AND " +
           "(:company IS NULL OR LOWER(v.company) = LOWER(:company)) AND " +
           "(:query IS NULL OR :query = '' OR " +
           " LOWER(v.vehicleNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(v.company) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(v.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(v.model) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Vehicle> searchVehicles(
            @Param("query") String query,
            @Param("status") VehicleStatus status,
            @Param("company") String company,
            Sort sort
    );

    @Query("SELECT v FROM Vehicle v WHERE " +
           "(:status IS NULL OR v.status = :status) AND " +
           "(:type IS NULL OR v.type = :type) AND " +
           "(:vehicleNumber IS NULL OR LOWER(v.vehicleNumber) LIKE LOWER(CONCAT('%', :vehicleNumber, '%'))) AND " +
           "(:company IS NULL OR LOWER(v.company) LIKE LOWER(CONCAT('%', :company, '%'))) AND " +
           "(:brand IS NULL OR LOWER(v.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
           "(:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))) AND " +
           "(:query IS NULL OR " +
           " LOWER(v.vehicleNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(v.company) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(v.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(v.model) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Vehicle> advancedSearchVehicles(
            @Param("query") String query,
            @Param("vehicleNumber") String vehicleNumber,
            @Param("company") String company,
            @Param("type") VehicleType type,
            @Param("brand") String brand,
            @Param("model") String model,
            @Param("status") VehicleStatus status,
            Sort sort
    );

    @Query("SELECT v FROM Vehicle v WHERE " +
           "(:status IS NULL OR v.status = :status) AND " +
           "(:type IS NULL OR v.type = :type) AND " +
           "(:vehicleNumber IS NULL OR LOWER(v.vehicleNumber) LIKE LOWER(CONCAT('%', :vehicleNumber, '%'))) AND " +
           "(:company IS NULL OR LOWER(v.company) LIKE LOWER(CONCAT('%', :company, '%'))) AND " +
           "(:brand IS NULL OR LOWER(v.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
           "(:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))) AND " +
           "(:query IS NULL OR " +
           " LOWER(v.vehicleNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(v.company) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(v.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(v.model) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Vehicle> findFilteredVehicles(
            @Param("query") String query,
            @Param("vehicleNumber") String vehicleNumber,
            @Param("company") String company,
            @Param("type") VehicleType type,
            @Param("brand") String brand,
            @Param("model") String model,
            @Param("status") VehicleStatus status,
            Pageable pageable
    );
}

