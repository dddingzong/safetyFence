package com.project.safetyFence.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateDto {


    // 서버에서 세션으로부터 자동 설정
    private String userNumber;
    private Double latitude;
    private Double longitude;
    private Long timestamp;

    public LocationUpdateDto(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = System.currentTimeMillis();
    }
}
