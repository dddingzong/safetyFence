package com.project.safetyFence.domain.dto.request;

import lombok.Getter;

@Getter
public class EventDataRequestDto {
    private String number;
    private String event;
    private String eventDate;
    private String startTime;

    public EventDataRequestDto(String number, String event, String eventDate, String startTime) {
        this.number = number;
        this.event = event;
        this.eventDate = eventDate;
        this.startTime = startTime;
    }
}
