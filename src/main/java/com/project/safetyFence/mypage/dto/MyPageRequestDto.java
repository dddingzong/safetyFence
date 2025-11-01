package com.project.safetyFence.mypage.dto;

import com.project.safetyFence.geofence.dto.SimpleGeofenceResponseDto;
import lombok.Getter;

import java.util.List;

@Getter
public class MyPageRequestDto {
    private String name;
    private String number;
    private String birth;
    private String linkCode;
    private String userAddress;
    private String centerAddress;
    private List<SimpleGeofenceResponseDto> geofenceList;

}
