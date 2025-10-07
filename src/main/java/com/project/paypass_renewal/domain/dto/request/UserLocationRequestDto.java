package com.project.paypass_renewal.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserLocationRequestDto {

    private String number;
    private String latitude;
    private String longitude;

    public UserLocationRequestDto(String number, String latitude, String longitude) {
        this.number = number;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
