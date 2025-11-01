package com.project.safetyFence.controller;

import com.project.safetyFence.domain.dto.request.LocationUpdateDto;
import com.project.safetyFence.service.LocationCacheService;
import com.project.safetyFence.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LocationWebSocketController {

    private final LocationCacheService cacheService;
    private final LocationService locationService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 클라이언트 → 서버: /app/location
     * 서버 → 구독자들: /topic/location/{userNumber}
     *
     * @param location 위치 정보 (lat, lng)
     * @param sessionAttributes WebSocket 세션 속성 (userNumber 포함)
     */
    @MessageMapping("/location")
    public void updateLocation(
            LocationUpdateDto location,
            @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        // 세션에서 사용자 번호 추출
        String userNumber = (String) sessionAttributes.get("userNumber");

        if (userNumber == null) {
            log.error("세션에 userNumber가 없습니다.");
            return;
        }

        // DTO에 사용자 정보 설정
        location.setUserNumber(userNumber);
        location.setTimestamp(System.currentTimeMillis());

        log.debug("위치 업데이트 수신: userNumber={}, lat={}, lng={}",
                userNumber, location.getLatitude(), location.getLongitude());

        // 1. 캐시에 최신 위치 저장
        cacheService.updateLocation(userNumber, location);

        // 2. 해당 사용자를 구독 중인 모든 클라이언트에게 전송
        messagingTemplate.convertAndSend(
                "/topic/location/" + userNumber,
                location
        );

        // 3. 조건부 DB 저장 (비동기)
        locationService.saveLocationIfNeeded(location);
    }


}
