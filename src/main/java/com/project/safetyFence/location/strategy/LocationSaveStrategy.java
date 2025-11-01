package com.project.safetyFence.location.strategy;

import com.project.safetyFence.location.domain.UserLocation;
import com.project.safetyFence.location.dto.LocationUpdateDto;


public interface LocationSaveStrategy {
    boolean shouldSave(UserLocation previous, LocationUpdateDto current);
}
