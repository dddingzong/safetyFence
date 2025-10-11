package com.project.safetyFence.repository;

import com.project.safetyFence.domain.data.BusNumber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusNumberRepository extends JpaRepository<BusNumber, Long> {

    BusNumber findByRouteId(String routeId);

}
