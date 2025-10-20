package com.project.safetyFence.repository;

import com.project.safetyFence.domain.Geofence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeofenceRepository extends JpaRepository<Geofence, Long> {
}
