package com.project.safetyFence.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NumberRequestDto {

    private String number;

    public NumberRequestDto(String number) {
        this.number = number;
    }
}
