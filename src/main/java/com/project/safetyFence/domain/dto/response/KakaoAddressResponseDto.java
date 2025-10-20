package com.project.safetyFence.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class KakaoAddressResponseDto {

    @JsonProperty("documents")
    private List<DocumentDto> documents;

    public DocumentDto getFirstDocument() {
        return documents.get(0);
    }

    @Getter
    @NoArgsConstructor
    public static class DocumentDto {

        @JsonProperty("x")
        private String longitude; // 경도

        @JsonProperty("y")
        private String latitude;  // 위도
    }
}
