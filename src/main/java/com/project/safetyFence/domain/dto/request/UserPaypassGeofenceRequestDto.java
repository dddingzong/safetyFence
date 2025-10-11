package com.project.safetyFence.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserPaypassGeofenceRequestDto {

    private String number;
    private Long stationNumber;
    private String name;

    public UserPaypassGeofenceRequestDto(String number, Long stationNumber, String name) {
        this.number = number;
        this.stationNumber = stationNumber;
        this.name = name;
    }
}
