package com.project.safetyFence.geofence.handler;

import com.project.safetyFence.geofence.domain.Geofence;
import com.project.safetyFence.user.domain.User;

public interface GeofenceEntryHandler {


    void handle(User user, Geofence geofence);
    boolean supports(int type);
}
