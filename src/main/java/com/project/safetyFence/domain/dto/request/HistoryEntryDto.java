package com.project.safetyFence.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class HistoryEntryDto {
    private String name;
    private LocalDateTime time;

    public HistoryEntryDto(String name, LocalDateTime time) {
        this.name = name;
        this.time = time;
    }
}
