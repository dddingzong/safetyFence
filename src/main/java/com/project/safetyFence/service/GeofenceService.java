package com.project.safetyFence.service;

import com.project.safetyFence.domain.Geofence;
import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.UserAddress;
import com.project.safetyFence.domain.dto.request.NumberRequestDto;
import com.project.safetyFence.domain.dto.response.KakaoAddressResponseDto;
import com.project.safetyFence.repository.GeofenceRepository;
import com.project.safetyFence.repository.UserRepository;
import com.project.safetyFence.util.kakaoApi.KakaoApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeofenceService {

    private final GeofenceRepository geofenceRepository;
    private final KakaoApiService kakaoApiService; // KakaoApiService 의존성 주입
    private final UserRepository userRepository;

    public Geofence saveInitialHomeGeofence(UserAddress userAddress) {
        // 1. UserAddress에서 도로명 주소 문자열을 가져옵니다.
        String address = userAddress.getHomeStreetAddress();
        // 2. Kakao API를 호출하여 주소를 좌표로 변환합니다.
        KakaoAddressResponseDto.DocumentDto document = kakaoApiService.requestAddressSearch(address);

        BigDecimal latitude = new BigDecimal(document.getLatitude());
        BigDecimal longitude = new BigDecimal(document.getLongitude());

        Geofence geofence = new Geofence(userAddress.getUser(), "집", address ,latitude, longitude, 0, 100);
        return geofence;
    }

    public Geofence saveInitialCenterGeofence(UserAddress userAddress) {
        String address = userAddress.getCenterStreetAddress();
        KakaoAddressResponseDto.DocumentDto document = kakaoApiService.requestAddressSearch(address);

        BigDecimal latitude = new BigDecimal(document.getLatitude());
        BigDecimal longitude = new BigDecimal(document.getLongitude());

        Geofence geofence = new Geofence(userAddress.getUser(), "센터", address ,latitude, longitude, 0, 100);
        return geofence;
    }

    @Transactional
    public List<Geofence> findGeofenceByNumber(NumberRequestDto numberRequestDto) {
        User user = userRepository.findByNumber(numberRequestDto.getNumber());
        List<Geofence> geofences = geofenceRepository.findByUser(user);
        return geofences;
    }

}
