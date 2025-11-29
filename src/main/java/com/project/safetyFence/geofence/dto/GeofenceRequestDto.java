package com.project.safetyFence.geofence.dto;

import lombok.Getter;

@Getter
public class GeofenceRequestDto {
    private String name;
    private String address;
    private int type;
    private String startTime;
    private String endTime;
    private String number;

    public GeofenceRequestDto(String name, String address, int type, String startTime, String endTime, String number) {
        this.name = name;
        this.address = address;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.number = number;
    }
}
