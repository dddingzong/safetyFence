package com.project.safetyFence.repository;

import com.project.safetyFence.domain.UserCareGeofence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCareGeofenceRepository extends JpaRepository<UserCareGeofence, Long> {

    UserCareGeofence findByNumber(String number);
}
