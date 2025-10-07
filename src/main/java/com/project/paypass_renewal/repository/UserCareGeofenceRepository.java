package com.project.paypass_renewal.repository;

import com.project.paypass_renewal.domain.UserCareGeofence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCareGeofenceRepository extends JpaRepository<UserCareGeofence, Long> {

    UserCareGeofence findByNumber(String number);
}
