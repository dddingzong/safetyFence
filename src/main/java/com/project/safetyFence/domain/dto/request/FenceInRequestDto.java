package com.project.safetyFence.domain.dto.request;

import lombok.Getter;

@Getter
public class FenceInRequestDto {
    private Long geofenceId;
    private String number;

    public FenceInRequestDto(Long geofenceId, String number) {
        this.geofenceId = geofenceId;
        this.number = number;
    }
}
