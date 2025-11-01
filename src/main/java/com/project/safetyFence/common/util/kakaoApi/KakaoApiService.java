package com.project.safetyFence.common.util.kakaoApi;

import com.project.safetyFence.common.util.kakaoApi.dto.KakaoAddressResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Component
public class KakaoApiService {

    private final RestTemplate restTemplate;
    private final String kakaoApiKey;

    public KakaoApiService(RestTemplateBuilder restTemplateBuilder, @Value("${kakao.rest.api-key}") String kakaoApiKey) {
        this.kakaoApiKey = kakaoApiKey;
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * 주소를 받아서 카카오 API를 통해 좌표(경도, 위도)를 포함한 Document를 동기적으로 반환합니다.
     * @param address 변환할 주소
     * @return 좌표 정보가 담긴 DocumentDto. 결과가 없거나 에러 발생 시 null.
     */
    public KakaoAddressResponseDto.DocumentDto requestAddressSearch(String address) {
        log.info("위도 경도로 변환할 입력받은 값: {}", address);

        URI uri = UriComponentsBuilder
                .fromUriString("https://dapi.kakao.com")
                .path("/v2/local/search/address.json")
                .queryParam("query", address)
                .encode()
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoAddressResponseDto> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    KakaoAddressResponseDto.class
            );

            if (response.getBody() == null) {
                log.warn("Response 에러 발생 : {}", address);
                return null;
            }

            KakaoAddressResponseDto.DocumentDto document = response.getBody().getFirstDocument();
            log.info("성공적으로 위도와 경도를 반환했습니다. '{}': [Longitude={}, Latitude={}]", address, document.getLongitude(), document.getLatitude());
            return document;

        } catch (HttpClientErrorException e) {
            log.error("Error during Kakao API call for address: {}. Status: {}, Body: {}", address, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("An unexpected error occurred during Kakao API call for address: {}", address, e);
            return null;
        }
    }
}