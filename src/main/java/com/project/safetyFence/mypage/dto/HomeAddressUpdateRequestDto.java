package com.project.safetyFence.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HomeAddressUpdateRequestDto {
    private String homeAddress;  // 우편번호
    private String homeStreetAddress;
    private String homeStreetAddressDetail;
}
