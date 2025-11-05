package com.project.safetyFence.location.strategy;

import com.project.safetyFence.location.domain.UserLocation;
import com.project.safetyFence.location.dto.LocationUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DistanceBasedSaveStrategy implements LocationSaveStrategy {

    private static final double EARTH_RADIUS = 6371000; // 지구 반지름 (미터)
    private static final double MIN_DISTANCE_METERS = 100.0;  // 최소 이동 거리 (미터)
    private static final long MIN_TIME_DIFF_MILLIS = 60_000;  // 최소 시간 차이 (1분)

    @Override
    public boolean shouldSave(UserLocation previous, LocationUpdateDto current) {
        // 이전 위치가 없으면 무조건 저장
        if (previous == null) {
            log.info("첫 위치 저장: userNumber={}", current.getUserNumber());
            return true;
        }

        // 거리 계산
        double distance = calculateDistance(
                previous.getLatitude(),
                previous.getLongitude(),
                current.getLatitude(),
                current.getLongitude()
        );

        // 시간 차이 계산
        long timeDiff = current.getTimestamp() -
                previous.getSavedTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

        // 거리 조건 확인
        if (distance >= MIN_DISTANCE_METERS) {
            log.info("거리 조건 충족하여 저장: userNumber={}, distance={}m",
                    current.getUserNumber(), String.format("%.2f", distance));
            return true;
        }

        // 시간 조건 확인
        if (timeDiff >= MIN_TIME_DIFF_MILLIS) {
            log.info("시간 조건 충족하여 저장: userNumber={}, timeDiff={}초",
                    current.getUserNumber(), timeDiff / 1000);
            return true;
        }

        // 조건 미충족
        log.debug("저장 조건 미충족: userNumber={}, distance={}m, timeDiff={}초",
                current.getUserNumber(), String.format("%.2f", distance), timeDiff / 1000);
        return false;
    }

    /**
     * Haversine 공식을 사용한 거리 계산
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
