package com.project.safetyFence.domain.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class UserCareGeofenceResponseDto {
    private String number;
    private BigDecimal homeLatitude;
    private BigDecimal homeLongitude;
    private BigDecimal centerLatitude;
    private BigDecimal centerLongitude;

    public UserCareGeofenceResponseDto(String number, BigDecimal homeLatitude, BigDecimal homeLongitude, BigDecimal centerLatitude, BigDecimal centerLongitude) {
        this.number = number;
        this.homeLatitude = homeLatitude;
        this.homeLongitude = homeLongitude;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
    }
}
