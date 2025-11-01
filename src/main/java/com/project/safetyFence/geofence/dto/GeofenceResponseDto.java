package com.project.safetyFence.geofence.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class GeofenceResponseDto {
    private Long id;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private int type; // 0: 영구, 1: 일시
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int maxSequence;

    public GeofenceResponseDto(Long id, String name, String address, BigDecimal latitude, BigDecimal longitude, int type, LocalDateTime startTime, LocalDateTime endTime, int maxSequence) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxSequence = maxSequence;
    }
}
