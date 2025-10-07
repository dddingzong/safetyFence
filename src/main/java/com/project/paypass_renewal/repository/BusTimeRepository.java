package com.project.paypass_renewal.repository;

import com.project.paypass_renewal.domain.data.BusTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusTimeRepository extends JpaRepository<BusTime, Long> {

    List<BusTime> findByRouteId(String routeId);
}
