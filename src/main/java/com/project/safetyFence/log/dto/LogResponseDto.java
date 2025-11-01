package com.project.safetyFence.log.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LogResponseDto {
    private Long id;
    private String location;
    private String locationAddress;
    private String arriveTime;

    public LogResponseDto(Long id, String location, String locationAddress, String arriveTime) {
        this.id = id;
        this.location = location;
        this.locationAddress = locationAddress;
        this.arriveTime = arriveTime;
    }
}
