package com.project.paypass_renewal.domain.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class UserLatLngListResponseDto {
    private BigDecimal latitude;
    private BigDecimal longitude;

    public UserLatLngListResponseDto(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
