package com.project.safetyFence.user.dto;

import lombok.Getter;

@Getter
public class SignInRequestDto {
    private String number;
    private String password;

    public SignInRequestDto(String number, String password) {
        this.number = number;
        this.password = password;
    }
}
