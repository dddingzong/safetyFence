package com.project.safetyFence.location;

import com.project.safetyFence.user.domain.User;
import com.project.safetyFence.location.domain.UserLocation;
import com.project.safetyFence.location.dto.LocationUpdateDto;
import com.project.safetyFence.location.UserLocationRepository;
import com.project.safetyFence.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final double EARTH_RADIUS = 6371000;

    private final UserRepository userRepository;
    private final UserLocationRepository userLocationRepository;

    // 저장 조건
    private static final double MIN_DISTANCE_METERS = 100.0;  // 최소 이동 거리 (미터)
    private static final long MIN_TIME_DIFF_MILLIS = 60_000;  // 최소 시간 차이 (1분)

    @Async
    @Transactional
    public void saveLocationIfNeeded(LocationUpdateDto locationDto) {
        try {
            // 사용자 조회
            User user = userRepository.findByNumber(locationDto.getUserNumber());
            if (user == null) {
                log.warn("사용자를 찾을 수 없습니다: userNumber={}", locationDto.getUserNumber());
                return;
            }

            // 이전 위치 조회
            Optional<UserLocation> previousLocationOpt = userLocationRepository.findLatestByUser(user);

            // 이전 위치가 없으면 무조건 저장
            if (previousLocationOpt.isEmpty()) {
                saveLocation(user, locationDto);
                log.info("첫 위치 저장: userNumber={}", locationDto.getUserNumber());
                return;
            }

            UserLocation previousLocation = previousLocationOpt.get();

            // 거리 계산
            double distance = calculateDistance(
                    previousLocation.getLatitude().doubleValue(),
                    previousLocation.getLongitude().doubleValue(),
                    locationDto.getLatitude(),
                    locationDto.getLongitude()
            );

            // 시간 차이 계산
            long timeDiff = locationDto.getTimestamp() -
                    previousLocation.getSavedTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

            // 저장 조건 확인
            if (distance >= MIN_DISTANCE_METERS) {
                saveLocation(user, locationDto);
                log.info("거리 조건 충족하여 저장: userNumber={}, distance={}m",
                        locationDto.getUserNumber(), String.format("%.2f", distance));
            } else if (timeDiff >= MIN_TIME_DIFF_MILLIS) {
                saveLocation(user, locationDto);
                log.info("시간 조건 충족하여 저장: userNumber={}, timeDiff={}초",
                        locationDto.getUserNumber(), timeDiff / 1000);
            } else {
                log.debug("저장 조건 미충족: userNumber={}, distance={}m, timeDiff={}초",
                        locationDto.getUserNumber(), String.format("%.2f", distance), timeDiff / 1000);
            }

        } catch (Exception e) {
            log.error("위치 저장 중 오류 발생: userNumber={}", locationDto.getUserNumber(), e);
        }
    }

    private void saveLocation(User user, LocationUpdateDto locationDto) {
        UserLocation userLocation = new UserLocation(
                user,
                BigDecimal.valueOf(locationDto.getLatitude()),
                BigDecimal.valueOf(locationDto.getLongitude())
        );

        user.addUserLocation(userLocation);
    }


    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
