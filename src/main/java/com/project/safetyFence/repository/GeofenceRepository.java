package com.project.safetyFence.repository;

import com.project.safetyFence.domain.Geofence;
import com.project.safetyFence.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeofenceRepository extends JpaRepository<Geofence, Long> {

    List<Geofence> findByUser(User user);
}
