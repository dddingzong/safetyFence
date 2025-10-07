package com.project.paypass_renewal.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class DetailLogPathRequestDto {
    private String number;
    private LocalDateTime fenceInTime;
    private LocalDateTime fenceOutTime;

    public DetailLogPathRequestDto(String number, LocalDateTime fenceInTime, LocalDateTime fenceOutTime) {
        this.number = number;
        this.fenceInTime = fenceInTime;
        this.fenceOutTime = fenceOutTime;
    }
}
