package com.project.safetyFence.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CenterAddressUpdateRequestDto {
    private String centerAddress;  // 우편번호
    private String centerStreetAddress;
}
