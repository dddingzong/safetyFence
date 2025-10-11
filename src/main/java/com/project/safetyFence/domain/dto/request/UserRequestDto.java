package com.project.safetyFence.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.safetyFence.domain.ServiceCode;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UserRequestDto {

    @NotEmpty(message = "이름은 필수 값입니다.")
    @Pattern(regexp = "^[가-힣]{2,4}$", message = "이름은 한글로 2~4자 사이로 작성해주세요")
    private String name;

    @NotEmpty(message = "비밀번호는 필수 값입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$",
            message = "비밀번호는 영문자와 숫자를 포함한 6자리 이상이어야 합니다.")
    private String password;

    // LocalDate 타입은 @Valid 사용 불가, 따로 예외처리 진행
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Past(message = "생년월일은 과거 날짜여야 합니다")
    @NotNull(message = "생년월일은 필수 값입니다.")
    private LocalDate birth;

    @NotEmpty(message = "전화번호는 필수 값입니다.")
    @Pattern(regexp = "^\\d{11}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String number;

    @NotEmpty(message = "집 주소는 필수 값입니다.")
    @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
    private String homeAddress;

    @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다. 거주 센터가 없으시다면 빈칸으로 제출해 주십시오")
    private String centerAddress;

    @NotNull(message = "서비스 코드는 필수 값입니다.")
    private ServiceCode serviceCode;

    @NotEmpty(message = "집 주소는 필수 값입니다.")
    private String homeStreetAddress;

    @NotEmpty(message = "집 상세 주소는 필수 값입니다.")
    private String homeStreetAddressDetail;

    private String centerStreetAddress;

    public UserRequestDto(String name, String password, LocalDate birth, String number, String homeAddress, String centerAddress, ServiceCode serviceCode, String homeStreetAddress, String homeStreetAddressDetail, String centerStreetAddress) {
        this.name = name;
        this.password = password;
        this.birth = birth;
        this.number = number;
        this.homeAddress = homeAddress;
        this.centerAddress = centerAddress;
        this.serviceCode = serviceCode;
        this.homeStreetAddress = homeStreetAddress;
        this.homeStreetAddressDetail = homeStreetAddressDetail;
        this.centerStreetAddress = centerStreetAddress;
    }
}
