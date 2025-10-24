package com.project.safetyFence.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LogItemDto {
    private Long logId;
    private String location;
    private String locationAddress;
    private String arriveTime;
}
