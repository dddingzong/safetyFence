package com.project.paypass_renewal.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StationNumberRequestDto {
    private Long stationNumber;

    public StationNumberRequestDto(Long stationNumber) {
        this.stationNumber = stationNumber;
    }

}
