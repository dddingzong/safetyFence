package com.project.safetyFence.service;

import com.project.safetyFence.domain.dto.response.KakaoAddressResponseDto;
import com.project.safetyFence.util.kakaoApi.KakaoApiService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
class KakaoApiServiceLiveTest {

    @Autowired
    private KakaoApiService kakaoApiService;

    @Value("${kakao.rest.api-key}")
    private String kakaoApiKey;

    @Test
    @DisplayName("실제 카카오 API 연동 테스트")
    void kakaoApi_LiveTest_Success() {
        // given: 실제 API 키가 설정되어 있는지 확인 (없으면 테스트 스킵)
        assumeTrue(kakaoApiKey != null && !kakaoApiKey.equals("YOUR_KAKAO_REST_API_KEY"),
                "실제 카카오 API 키가 설정되지 않아 테스트를 건너뜁니다.");
        System.out.println("주소: ");

        String address = "서울 강남구 테헤란로 212";

        // when: 실제 API 호출
        KakaoAddressResponseDto.DocumentDto document = kakaoApiService.requestAddressSearch(address);

        // then: 응답이 정상적으로 오는지 확인
        assertThat(document).isNotNull();
        assertThat(document.getLatitude()).isNotBlank();
        assertThat(document.getLongitude()).isNotBlank();

        System.out.println("주소: " + address);
        System.out.println("위도: " + document.getLatitude());
        System.out.println("경도: " + document.getLongitude());
    }
}
