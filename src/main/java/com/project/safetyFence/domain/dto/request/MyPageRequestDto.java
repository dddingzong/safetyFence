package com.project.safetyFence.domain.dto.request;

import com.project.safetyFence.domain.dto.response.SimpleGeofenceResponseDto;
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
