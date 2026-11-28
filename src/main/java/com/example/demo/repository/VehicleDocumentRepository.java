package com.example.demo.repository;

import com.example.demo.model.entity.VehicleDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleDocumentRepository extends JpaRepository<VehicleDocument, String> {

    List<VehicleDocument> findByVehicleIdOrderByUploadDateDesc(String vehicleId);

    Optional<VehicleDocument> findByIdAndVehicleId(String id, String vehicleId);

    @Query("SELECT d FROM VehicleDocument d JOIN FETCH d.vehicle " +
           "WHERE d.expiryDate IS NOT NULL AND d.expiryDate < :referenceDate " +
           "ORDER BY d.expiryDate ASC")
    List<VehicleDocument> findExpiredDocuments(@Param("referenceDate") LocalDate referenceDate);

    @Query("SELECT d FROM VehicleDocument d JOIN FETCH d.vehicle " +
           "WHERE d.expiryDate IS NOT NULL AND d.expiryDate >= :startDate AND d.expiryDate <= :endDate " +
           "ORDER BY d.expiryDate ASC")
    List<VehicleDocument> findExpiringSoonDocuments(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT d FROM VehicleDocument d JOIN FETCH d.vehicle " +
           "WHERE d.expiryDate IS NOT NULL AND d.expiryDate <= :endDate " +
           "ORDER BY d.expiryDate ASC")
    List<VehicleDocument> findExpiryAlerts(@Param("endDate") LocalDate endDate);

    @Query("SELECT d FROM VehicleDocument d JOIN FETCH d.vehicle " +
           "WHERE d.vehicle.id = :vehicleId AND d.expiryDate IS NOT NULL " +
           "ORDER BY d.expiryDate ASC")
    List<VehicleDocument> findByVehicleIdWithExpiryDate(@Param("vehicleId") String vehicleId);

    @Query("SELECT COUNT(d) FROM VehicleDocument d WHERE d.expiryDate IS NOT NULL AND d.expiryDate < :referenceDate")
    long countExpiredDocuments(@Param("referenceDate") java.time.LocalDate referenceDate);

    @Query("SELECT COUNT(d) FROM VehicleDocument d WHERE d.expiryDate IS NOT NULL AND d.expiryDate >= :startDate AND d.expiryDate <= :endDate")
    long countExpiringSoonDocuments(@Param("startDate") java.time.LocalDate startDate,
                                    @Param("endDate") java.time.LocalDate endDate);
}
