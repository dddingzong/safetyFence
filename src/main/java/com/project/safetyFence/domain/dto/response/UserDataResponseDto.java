package com.project.safetyFence.domain.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class UserDataResponseDto {
    private String name;
    private String birth;
    private String homeAddress;
    private String centerAddress;
    private String linkCode;
    private List<SimpleGeofenceResponseDto> geofences;

    public UserDataResponseDto(String name, String birth, String homeAddress, String centerAddress, String linkCode, List<SimpleGeofenceResponseDto> geofences) {
        this.name = name;
        this.birth = birth;
        this.homeAddress = homeAddress;
        this.centerAddress = centerAddress;
        this.linkCode = linkCode;
        this.geofences = geofences;
    }
}
