package com.project.safetyFence.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserEventItemDto {
    private Long userEventId;
    private String event;
    private String eventStartTime;
}
