package com.project.safetyFence.domain.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyPageResponseDto {
    private String name;
    private String number;
    private String homeStreetAddress;
    private String homeStreetAddressDetail;
    private String centerStreetAddress;
    private String linkCode;

    public MyPageResponseDto(String name, String number, String homeStreetAddress, String homeStreetAddressDetail, String centerStreetAddress, String linkCode) {
        this.name = name;
        this.number = number;
        this.homeStreetAddress = homeStreetAddress;
        this.homeStreetAddressDetail = homeStreetAddressDetail;
        this.centerStreetAddress = centerStreetAddress;
        this.linkCode = linkCode;
    }
}
