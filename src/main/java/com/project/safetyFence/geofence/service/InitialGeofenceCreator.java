package com.project.safetyFence.geofence.service;

import com.project.safetyFence.geofence.domain.Geofence;
import com.project.safetyFence.user.domain.UserAddress;

public interface InitialGeofenceCreator {

    Geofence createHomeGeofence(UserAddress userAddress);
    Geofence createCenterGeofence(UserAddress userAddress);

}
