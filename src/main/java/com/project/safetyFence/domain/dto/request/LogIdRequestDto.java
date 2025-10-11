package com.project.safetyFence.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LogIdRequestDto {
    private Long logId;

    public LogIdRequestDto(Long logId) {
        this.logId = logId;
    }

}
