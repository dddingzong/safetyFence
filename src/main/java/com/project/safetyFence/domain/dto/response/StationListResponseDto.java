package com.project.safetyFence.domain.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
@NoArgsConstructor
public class StationListResponseDto {
    private String name;
    private Long stationNumber;
    private BigDecimal longitude;
    private BigDecimal latitude;

    public StationListResponseDto(String name, Long stationNumber, BigDecimal longitude, BigDecimal latitude) {
        this.name = name;
        this.stationNumber = stationNumber;
        this.longitude = longitude;
        this.latitude = latitude;
    }

}
