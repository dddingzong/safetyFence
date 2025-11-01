package com.project.safetyFence.location;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.project.safetyFence.location.dto.LocationUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LocationCacheService {

    private final Cache<String, LocationUpdateDto> locationCache;

    public LocationCacheService() {
        this.locationCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build();

        log.info("LocationCacheService 초기화 완료: maxSize=10000, TTL=5분");
    }


    // 사용자의 최신 위치 업데이트
    public void updateLocation(String userNumber, LocationUpdateDto location) {
        locationCache.put(userNumber, location);
        log.debug("위치 캐시 업데이트: userNumber={}, lat={}, lng={}",
                userNumber, location.getLatitude(), location.getLongitude());
    }


    // 사용자의 최신 위치 조회
    public LocationUpdateDto getLatestLocation(String userNumber) {
        LocationUpdateDto location = locationCache.getIfPresent(userNumber);

        if (location != null) {
            log.debug("위치 캐시 조회 성공: userNumber={}", userNumber);
        } else {
            log.debug("위치 캐시 조회 실패 (캐시 없음): userNumber={}", userNumber);
        }

        return location;
    }

    // 사용자의 위치 캐시 삭제
    // WebSocket 연결 해제 시 호출
    public void removeLocation(String userNumber) {
        locationCache.invalidate(userNumber);
        log.info("위치 캐시 삭제: userNumber={}", userNumber);
    }


    // 캐시 통계 조회 (모니터링용)
    public String getCacheStats() {
        return locationCache.stats().toString();
    }

    // 캐시 크기 조회
    public long getCacheSize() {
        return locationCache.estimatedSize();
    }
}
