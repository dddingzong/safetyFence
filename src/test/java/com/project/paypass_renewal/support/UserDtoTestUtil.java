package com.project.paypass_renewal.support;

import com.project.paypass_renewal.domain.ServiceCode;
import com.project.paypass_renewal.domain.dto.request.UserRequestDto;

import java.time.LocalDate;

public class UserDtoTestUtil {

    public static UserRequestDto createDummyUserDto() {
        return new UserRequestDto("더미유저",
                "abc123",
                LocalDate.of(2000, 1, 1),
                "01012345678",
                "12345",
                "67890",
                ServiceCode.CARE_SERVICE,
                "서울시 노원구 노원로 564",
                "1001-102",
                "서울 노원구 노원로18길 41");
    }
}
