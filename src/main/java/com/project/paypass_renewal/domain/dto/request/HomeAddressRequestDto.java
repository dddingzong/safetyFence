package com.project.paypass_renewal.domain.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HomeAddressRequestDto {

    private String number;

    @NotEmpty(message = "집 주소는 필수 값입니다.")
    @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
    private String homeAddress;

    @NotEmpty(message = "집 주소는 필수 값입니다.")
    private String homeStreetAddress;

    @NotEmpty(message = "집 상세 주소는 필수 값입니다.")
    private String homeStreetAddressDetail;

    public HomeAddressRequestDto(String number, String homeAddress, String homeStreetAddress, String homeStreetAddressDetail) {
        this.number = number;
        this.homeAddress = homeAddress;
        this.homeStreetAddress = homeStreetAddress;
        this.homeStreetAddressDetail = homeStreetAddressDetail;
    }
}
