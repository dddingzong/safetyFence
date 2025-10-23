package com.project.safetyFence.domain.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SimpleGeofenceResponseDto {
    private Long id;
    private String name;
    private String address;
    private int type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public SimpleGeofenceResponseDto(Long id, String name, String address, int type, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
