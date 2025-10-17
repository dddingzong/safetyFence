package com.project.safetyFence.util.algorithm;

import com.project.safetyFence.domain.UserCareGeofence;
import com.project.safetyFence.domain.dto.request.CareGeofenceMoveDto;
import com.project.safetyFence.domain.dto.request.HistoryEntryDto;
import com.project.safetyFence.repository.UserCareGeofenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CareGeofenceTimeAlgorithm {

    private final UserCareGeofenceRepository userCareGeofenceRepository;

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double SPEED_KMH = 20.0;

    public boolean careTimeAlgorithm(CareGeofenceMoveDto careGeofenceMoveDto) {
        String number = careGeofenceMoveDto.getNumber();

        List<HistoryEntryDto> history = careGeofenceMoveDto.getHistory();
        LocalDateTime departureTime = history.get(0).getTime();
        LocalDateTime arrivalTime = history.get(1).getTime();

//        UserCareGeofence userCareGeofence = userCareGeofenceRepository.findByNumber(number);

        BigDecimal homeLatitude = userCareGeofence.getHomeLatitude();
        BigDecimal homeLongitude = userCareGeofence.getHomeLongitude();
        BigDecimal centerLatitude = userCareGeofence.getCenterLatitude();
        BigDecimal centerLongitude = userCareGeofence.getCenterLongitude();

        double minutesBetweenAverage = calculateTimeByLatAndLog(
                homeLatitude, homeLongitude, centerLatitude, centerLongitude);

        long minutesBetween  = Duration.between(departureTime, arrivalTime).toMinutes();

        boolean result = minutesBetween <= minutesBetweenAverage + 15;

        log.info("평균 예측 시간: " + minutesBetweenAverage + "분, 실제 시간: " + minutesBetween + "분, 결과: " + result);

        return result;
    }

    private double calculateTimeByLatAndLog(
            BigDecimal homeLatitude, BigDecimal homeLongitude,
            BigDecimal centerLatitude, BigDecimal centerLongitude) {

        double lat1 = homeLatitude.doubleValue();
        double lon1 = homeLongitude.doubleValue();
        double lat2 = centerLatitude.doubleValue();
        double lon2 = centerLongitude.doubleValue();

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(radLat1) * Math.cos(radLat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS_KM * c;

        return distance / SPEED_KMH * 60; // 시간 단위로 변환 (분 단위)
    }




}
