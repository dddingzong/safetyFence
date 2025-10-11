package com.project.safetyFence.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LinkDeleteRequestDto {
    private String supporterNumber;
    private String userNumber;

    public LinkDeleteRequestDto(String supporterNumber, String userNumber) {
        this.supporterNumber = supporterNumber;
        this.userNumber = userNumber;
    }
}
