package com.project.paypass_renewal.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LinkRequestDto {
    private String supporterNumber;
    private String userLinkCode;
    private String relation;

    public LinkRequestDto(String supporterNumber, String userLinkCode, String relation) {
        this.supporterNumber = supporterNumber;
        this.userLinkCode = userLinkCode;
        this.relation = relation;
    }
}
