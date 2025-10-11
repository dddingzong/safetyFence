package com.project.safetyFence.domain.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CenterAddressRequestDto {
    private String number;

    @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다. 거주 센터가 없으시다면 빈칸으로 제출해 주십시오")
    private String centerAddress;

    private String centerStreetAddress;

    public CenterAddressRequestDto(String number, String centerAddress, String centerStreetAddress) {
        this.number = number;
        this.centerAddress = centerAddress;
        this.centerStreetAddress = centerStreetAddress;
    }
}
