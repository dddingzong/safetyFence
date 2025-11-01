package com.project.safetyFence.common.service.geocoding;

import com.project.safetyFence.common.service.geocoding.dto.Coordinate;
import com.project.safetyFence.common.util.kakaoApi.dto.KakaoAddressResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;

/**
 * Kakao Maps API를 사용하여 주소를 좌표로 변환하는 구현체
 */
@Slf4j
@Service
public class KakaoGeocodingServiceImpl implements GeocodingService {

    private final RestTemplate restTemplate;
    private final String kakaoApiKey;

    public KakaoGeocodingServiceImpl(RestTemplateBuilder restTemplateBuilder,
                                     @Value("${kakao.rest.api-key}") String kakaoApiKey) {
        this.kakaoApiKey = kakaoApiKey;
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public Coordinate convertAddressToCoordinate(String address) {
        log.info("위도 경도로 변환할 입력받은 값: {}", address);

        KakaoAddressResponseDto.DocumentDto document = requestAddressSearch(address);

        if (document == null) {
            throw new IllegalArgumentException("주소를 좌표로 변환할 수 없습니다: " + address);
        }

        BigDecimal latitude = new BigDecimal(document.getLatitude());
        BigDecimal longitude = new BigDecimal(document.getLongitude());

        log.info("성공적으로 위도와 경도를 반환했습니다. '{}': [Longitude={}, Latitude={}]",
                address, longitude, latitude);

        return new Coordinate(latitude, longitude);
    }

    private KakaoAddressResponseDto.DocumentDto requestAddressSearch(String address) {
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

            return response.getBody().getFirstDocument();

        } catch (HttpClientErrorException e) {
            log.error("Error during Kakao API call for address: {}. Status: {}, Body: {}",
                    address, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("An unexpected error occurred during Kakao API call for address: {}", address, e);
            return null;
        }
    }
}
