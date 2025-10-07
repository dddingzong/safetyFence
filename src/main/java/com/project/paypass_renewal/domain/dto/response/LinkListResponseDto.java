package com.project.paypass_renewal.domain.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LinkListResponseDto {
    private String userNumber;
    private String name;
    private String homeAddress;
    private String relation;

    public LinkListResponseDto(String userNumber, String name, String homeAddress, String relation) {
        this.userNumber = userNumber;
        this.name = name;
        this.homeAddress = homeAddress;
        this.relation = relation;
    }
}
