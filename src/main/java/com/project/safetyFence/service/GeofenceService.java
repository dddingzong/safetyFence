package com.project.safetyFence.service;

import com.project.safetyFence.domain.Geofence;
import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.UserAddress;
import com.project.safetyFence.domain.dto.request.FenceInRequestDto;
import com.project.safetyFence.domain.dto.request.GeofenceRequestDto;
import com.project.safetyFence.domain.dto.request.NumberRequestDto;
import com.project.safetyFence.repository.GeofenceRepository;
import com.project.safetyFence.repository.UserRepository;
import com.project.safetyFence.util.kakaoApi.KakaoApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.project.safetyFence.domain.dto.response.KakaoAddressResponseDto.*;

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
        DocumentDto document = kakaoApiService.requestAddressSearch(address);

        BigDecimal latitude = new BigDecimal(document.getLatitude());
        BigDecimal longitude = new BigDecimal(document.getLongitude());

        Geofence geofence = new Geofence(userAddress.getUser(), "집", address ,latitude, longitude, 0, 100);
        return geofence;
    }

    public Geofence saveInitialCenterGeofence(UserAddress userAddress) {
        String address = userAddress.getCenterStreetAddress();
        DocumentDto document = kakaoApiService.requestAddressSearch(address);

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

    @Transactional
    public void userFenceIn(FenceInRequestDto fenceInRequestDto) {
        User user = userRepository.findByNumber(fenceInRequestDto.getNumber());
        Geofence geofence = geofenceRepository.findById(fenceInRequestDto.getGeofenceId())
                .orElseThrow(() -> new IllegalArgumentException("Geofence not found"));

        // 일회성이면 삭제
        if (geofence.getType() == 1) {
            geofenceRepository.delete(geofence);
            log.info("일시적인 지오펜스 진입: 지오펜스 ID " + geofence.getId() + " 삭제됨.");
        }

        // 지속성이면 maxSequence 차감
        if (geofence.getType() == 0) {
            int currentMaxSequence = geofence.getMaxSequence();
            if (currentMaxSequence > 0) {
                geofence.decreaseMaxSequence();
            } else {
                log.info("지속적인 지오펜스 진입: 지오펜스 ID " + geofence.getId() + "의 maxSequence가 이미 0입니다.");
            }
        }
    }

    @Transactional
    public void createNewFence(GeofenceRequestDto geofenceRequestDto) {
        User user = userRepository.findByNumber(geofenceRequestDto.getNumber());
        String address = geofenceRequestDto.getAddress();
        DocumentDto document = kakaoApiService.requestAddressSearch(address);

        BigDecimal latitude = new BigDecimal(document.getLatitude());
        BigDecimal longitude = new BigDecimal(document.getLongitude());

        Geofence geofence;
        if (geofenceRequestDto.getType() == 0) { // 영구 지오펜스
            geofence = new Geofence(user, geofenceRequestDto.getName(), address, latitude, longitude, 0, 999);
        } else { // 일시 지오펜스
            geofence = new Geofence(user, geofenceRequestDto.getName(), address, latitude, longitude, 1,
                    java.time.LocalDateTime.parse(geofenceRequestDto.getStartTime()),
                    java.time.LocalDateTime.parse(geofenceRequestDto.getEndTime()), 100);
        }

        user.addGeofence(geofence);
        geofenceRepository.save(geofence);
    }

}
