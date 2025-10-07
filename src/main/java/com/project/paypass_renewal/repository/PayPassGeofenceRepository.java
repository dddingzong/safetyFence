package com.project.paypass_renewal.repository;

import com.project.paypass_renewal.domain.PaypassGeofence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PayPassGeofenceRepository extends JpaRepository<PaypassGeofence, Long> {
    List<PaypassGeofence> findByNumberAndStationNumber(String number, Long stationNumber);
    List<PaypassGeofence> findByNumber(String number);
    void deleteByNumber(String number);

    @Query("SELECT p FROM PaypassGeofence p WHERE p.fenceInTime = (SELECT MAX(p2.fenceInTime) FROM PaypassGeofence p2 WHERE p2.number = p.number)")
    List<PaypassGeofence> findMostRecentGeofenceLocationByFenceInTime();
}
