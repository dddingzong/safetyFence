package com.project.safetyFence.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDto {
    String number;
    String password;

    public LoginRequestDto(String number, String password) {
        this.number = number;
        this.password = password;
    }
}
