package com.project.safetyFence.link.dto;

import lombok.Getter;

@Getter
public class LinkResponseDto {
    private Long id;
    private String userNumber;
    private String relation;

    public LinkResponseDto(Long id, String userNumber, String relation) {
        this.id = id;
        this.userNumber = userNumber;
        this.relation = relation;
    }
}
