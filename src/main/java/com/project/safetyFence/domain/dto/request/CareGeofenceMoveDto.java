package com.project.safetyFence.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CareGeofenceMoveDto {
    private String number;
    private List<HistoryEntryDto> history;

    public CareGeofenceMoveDto(String number, List<HistoryEntryDto> history) {
        this.number = number;
        this.history = history;
    }
}


