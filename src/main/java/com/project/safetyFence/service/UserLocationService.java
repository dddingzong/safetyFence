package com.project.safetyFence.service;

import com.project.safetyFence.domain.UserLocation;
import com.project.safetyFence.domain.dto.request.DetailLogPathRequestDto;
import com.project.safetyFence.domain.dto.request.NumberRequestDto;
import com.project.safetyFence.domain.dto.request.UserLocationRequestDto;
import com.project.safetyFence.domain.dto.response.UserLocationResponseDto;
import com.project.safetyFence.repository.UserLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLocationService {

    private final UserLocationRepository userLocationRepository;

    public UserLocation saveUserLocation(UserLocationRequestDto userLocationDto){

        UserLocation userLocation = toEntity(userLocationDto);

        userLocationRepository.save(userLocation);

        return userLocation;
    }

    private UserLocation toEntity(UserLocationRequestDto userLocationDto){

        String number = userLocationDto.getNumber();
        String latitude = userLocationDto.getLatitude();
        String longitude = userLocationDto.getLongitude();

        return new UserLocation(number, new BigDecimal(latitude), new BigDecimal(longitude));
    }


    public UserLocationResponseDto findRecentLocationByNumber(NumberRequestDto numberRequestDto) {
        String number = numberRequestDto.getNumber();

        List<UserLocation> userLocations = userLocationRepository.findByNumberOrderBySavedTimeDesc(number);

        UserLocation userLocation = userLocations.get(0);
        BigDecimal latitude = userLocation.getLatitude();
        BigDecimal longitude = userLocation.getLongitude();

        return new UserLocationResponseDto(latitude, longitude);
    }

    public List<Map<String, BigDecimal>> getUserMovingPath(DetailLogPathRequestDto detailLogPathRequestDto) {
        List<Map<String, BigDecimal>> latLngList = new ArrayList<>();

        String number = detailLogPathRequestDto.getNumber();
        LocalDateTime fenceInTime = detailLogPathRequestDto.getFenceInTime();
        LocalDateTime fenceOutTime = detailLogPathRequestDto.getFenceOutTime();

        log.info("사용자 이동 경로 분석 변수 검사: number = " + number + ", fenceInTime = " + fenceInTime + ", fenceOutTime = " + fenceOutTime);

        List<UserLocation> userLocation = userLocationRepository.findByNumberAndSavedTimeBetweenOrderBySavedTime(number, fenceInTime, fenceOutTime);

        for (UserLocation location : userLocation) {
            BigDecimal latitude = location.getLatitude();
            BigDecimal longitude = location.getLongitude();
            Map<String, BigDecimal> latLngMap = Map.of("latitude", latitude, "longitude", longitude);
            latLngList.add(latLngMap);
        }

        return latLngList;
    }
}
