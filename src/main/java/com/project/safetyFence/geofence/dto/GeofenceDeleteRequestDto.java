package com.project.safetyFence.geofence.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GeofenceDeleteRequestDto {

    private Long id;

    public GeofenceDeleteRequestDto(Long id) {
        this.id = id;
    }
}
