package com.project.safetyFence.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EventDataRequestDto {
    private String event;
    private String eventDate;
    private String startTime;

    public EventDataRequestDto(String event, String eventDate, String startTime) {
        this.event = event;
        this.eventDate = eventDate;
        this.startTime = startTime;
    }
}
