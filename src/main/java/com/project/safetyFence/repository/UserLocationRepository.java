package com.project.safetyFence.repository;

import com.project.safetyFence.domain.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {

    List<UserLocation> findByNumberOrderBySavedTimeDesc(String number);
    List<UserLocation> findByNumberAndSavedTimeBetweenOrderBySavedTime(String number, LocalDateTime start, LocalDateTime end);

}
