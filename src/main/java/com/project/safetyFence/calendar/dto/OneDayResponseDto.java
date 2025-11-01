package com.project.safetyFence.calendar.dto;

import com.project.safetyFence.log.dto.LogItemDto;
import com.project.safetyFence.geofence.dto.GeofenceItemDto;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class OneDayResponseDto {
    private String date;
    private List<LogItemDto> logs;
    private List<GeofenceItemDto> geofences;
    private List<UserEventItemDto> userEvents;

    public OneDayResponseDto(String date) {
        this.date = date;
        this.logs = new ArrayList<>();
        this.geofences = new ArrayList<>();
        this.userEvents = new ArrayList<>();
    }

    public void addLog(LogItemDto log) {
        this.logs.add(log);
    }

    public void addGeofence(GeofenceItemDto geofence) {
        this.geofences.add(geofence);
    }

    public void addUserEvent(UserEventItemDto userEvent) {
        this.userEvents.add(userEvent);
    }
}
