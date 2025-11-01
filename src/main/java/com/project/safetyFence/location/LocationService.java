package com.project.safetyFence.location;

import com.project.safetyFence.user.domain.User;
import com.project.safetyFence.location.domain.UserLocation;
import com.project.safetyFence.location.dto.LocationUpdateDto;
import com.project.safetyFence.location.strategy.LocationSaveStrategy;
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

    private final UserRepository userRepository;
    private final UserLocationRepository userLocationRepository;
    private final LocationSaveStrategy locationSaveStrategy; // 전략 패턴 적용

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
            UserLocation previousLocation = previousLocationOpt.orElse(null);

            // 전략 패턴 적용: 저장 여부 판단을 전략 객체에 위임
            if (locationSaveStrategy.shouldSave(previousLocation, locationDto)) {
                saveLocation(user, locationDto);
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
}
