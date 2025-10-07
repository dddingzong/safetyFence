package com.project.paypass_renewal.support;

import com.project.paypass_renewal.domain.ServiceCode;
import com.project.paypass_renewal.domain.User;

import java.time.LocalDate;

public class UserTestUtils {

    public static User createDummyUser() {
        return new User("더미유저",
                "abc123",
                LocalDate.of(2000, 1, 1),
                "01012345678",
                "12345",
                "67890",
                "123456",
                ServiceCode.CARE_SERVICE);
    }

}
