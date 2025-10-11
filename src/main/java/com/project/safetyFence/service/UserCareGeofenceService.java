package com.project.safetyFence.service;

import com.project.safetyFence.domain.Log;
import com.project.safetyFence.domain.UserCareGeofence;
import com.project.safetyFence.domain.dto.request.CareGeofenceMoveDto;
import com.project.safetyFence.domain.dto.request.HistoryEntryDto;
import com.project.safetyFence.domain.dto.request.UserRequestDto;
import com.project.safetyFence.domain.dto.response.UserCareGeofenceResponseDto;
import com.project.safetyFence.repository.UserCareGeofenceRepository;
import com.project.safetyFence.util.ZipCodeToLatLogUtils;
import com.project.safetyFence.util.algorithm.CareGeofenceTimeAlgorithm;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCareGeofenceService {

    private final UserCareGeofenceRepository userCareGeofenceRepository;
    private final LogService logService;

    private final CareGeofenceTimeAlgorithm careGeofenceTimeAlgorithm;

    @Transactional
    public UserCareGeofence saveUserGeofence(UserRequestDto userRequestDto) {
        log.info("유저 저장으로 인해 집과 센터에 geofence를 생성합니다.");

        BigDecimal centerLatitude = null;
        BigDecimal centerLongitude = null;

        String homeAddress = userRequestDto.getHomeAddress();
        String centerAddress = userRequestDto.getCenterAddress();

        Map<String, BigDecimal> homeLatLog = ZipCodeToLatLogUtils.getLatLogFromZipCode(homeAddress);

        if (centerAddress != null) {
            Map<String, BigDecimal> centerLatLog = ZipCodeToLatLogUtils.getLatLogFromZipCode(centerAddress);
            centerLatitude = centerLatLog.get("latitude");
            centerLongitude = centerLatLog.get("longitude");
        }

        log.info("geofence 생성을 완료했습니다.");

        UserCareGeofence userCareGeofence = new UserCareGeofence(
                userRequestDto.getNumber(),
                homeLatLog.get("latitude"),
                homeLatLog.get("longitude"),
                centerLatitude,
                centerLongitude
        );

        userCareGeofenceRepository.save(userCareGeofence);

        return userCareGeofence;
    }

    @Transactional
    public UserCareGeofenceResponseDto findUserCareGeofence(String number) {

        UserCareGeofence userGeofence = userCareGeofenceRepository.findByNumber(number);

        String userNumber = userGeofence.getNumber();
        BigDecimal homeLatitude = userGeofence.getHomeLatitude();
        BigDecimal homeLongitude = userGeofence.getHomeLongitude();
        BigDecimal centerLatitude = userGeofence.getCenterLatitude();
        BigDecimal centerLongitude = userGeofence.getCenterLongitude();

        if (centerLatitude == null && centerLongitude == null) {
            log.info("센터 주소가 없으므로, null값을 반환합니다.");
            return null;
        }

        return new UserCareGeofenceResponseDto(userNumber, homeLatitude, homeLongitude, centerLatitude, centerLongitude);
    }

    public String checkCareGeofenceAlgorithm(CareGeofenceMoveDto careGeofenceMoveDto) {

        // 알고리즘 진행
        if(!careGeofenceTimeAlgorithm.careTimeAlgorithm(careGeofenceMoveDto)) return "notValidAlgorithm";

        String number = careGeofenceMoveDto.getNumber();

        List<HistoryEntryDto> history = careGeofenceMoveDto.getHistory();
        String departureLocation = history.get(0).getName();
        LocalDateTime departureTime = history.get(0).getTime();

        String arrivalLocation = history.get(1).getName();
        LocalDateTime arrivalTime = history.get(1).getTime();

        log.info("케어 지오펜스 알고리즘을 확인합니다. 출발지: {}, 도착지: {}, 출발 시간: {}, 도착 시간: {}",
                 departureLocation, arrivalLocation, departureTime, arrivalTime);

        logService.saveLog(new Log(number, departureTime, arrivalTime, departureLocation, arrivalLocation));

        return "successSaveLog";
    }
}
