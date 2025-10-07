package com.project.paypass_renewal.repository;

import com.project.paypass_renewal.domain.data.BusNumber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusNumberRepository extends JpaRepository<BusNumber, Long> {

    BusNumber findByRouteId(String routeId);

}
