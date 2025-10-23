package com.project.safetyFence.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FenceInRequestDto {
    private Long geofenceId;

    public FenceInRequestDto(Long geofenceId) {
        this.geofenceId = geofenceId;
    }
}
