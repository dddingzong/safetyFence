package com.project.safetyFence.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LinkRequestDto {
    private String linkCode;
    private String relation;

    public LinkRequestDto(String linkCode, String relation) {
        this.linkCode = linkCode;
        this.relation = relation;
    }
}
