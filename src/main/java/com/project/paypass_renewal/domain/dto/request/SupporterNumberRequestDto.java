package com.project.paypass_renewal.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SupporterNumberRequestDto {
    private String supporterNumber;

    public SupporterNumberRequestDto(String supporterNumber) {
        this.supporterNumber = supporterNumber;
    }
}
