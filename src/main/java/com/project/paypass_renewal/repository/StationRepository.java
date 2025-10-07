package com.project.paypass_renewal.repository;

import com.project.paypass_renewal.domain.data.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StationRepository extends JpaRepository<Station, Long> {
    @Query("SELECT s.busInfo FROM Station s WHERE s.stationNumber = :stationNumber")
    String findBusInfoByStationNumber(@Param("stationNumber") Long stationNumber);

    Station findByStationNumber(Long stationNumber);
}
