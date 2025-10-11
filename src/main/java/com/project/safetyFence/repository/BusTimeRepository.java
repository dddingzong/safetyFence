package com.project.safetyFence.repository;

import com.project.safetyFence.domain.data.BusTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusTimeRepository extends JpaRepository<BusTime, Long> {

    List<BusTime> findByRouteId(String routeId);
}
