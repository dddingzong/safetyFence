package com.project.safetyFence.geofence.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GeofenceItemDto {
    private Long geofenceId;
    private String name;
    private String address;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
