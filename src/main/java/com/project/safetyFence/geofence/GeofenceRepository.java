package com.project.safetyFence.geofence;

import com.project.safetyFence.geofence.domain.Geofence;
import com.project.safetyFence.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeofenceRepository extends JpaRepository<Geofence, Long> {

    List<Geofence> findByUser(User user);
}
