package com.project.safetyFence.controller;

import com.project.safetyFence.domain.dto.request.LocationUpdateDto;
import com.project.safetyFence.service.LinkService;
import com.project.safetyFence.service.LocationCacheService;
import com.project.safetyFence.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.nio.file.AccessDeniedException;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LocationWebSocketController {

    private final LinkService linkService;
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

    /**
     * 특정 사용자의 위치 구독 시 호출
     * @return 대상 사용자의 최신 위치
     */
    @SubscribeMapping("/topic/location/{targetUserNumber}")
    public LocationUpdateDto onSubscribe(
            @DestinationVariable String targetUserNumber,
            @Header("simpSessionAttributes") Map<String, Object> sessionAttributes
    ) throws AccessDeniedException {
        // 세션에서 구독자 번호 추출
        String subscriberNumber = (String) sessionAttributes.get("userNumber");

        if (subscriberNumber == null) {
            log.error("세션에 userNumber가 없습니다.");
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }

        log.info("위치 구독 요청: subscriber={}, target={}",
                subscriberNumber, targetUserNumber);

        // 권한 검증: 구독자가 대상 사용자를 Link로 등록했는지 확인
        if (!linkService.hasLink(subscriberNumber, targetUserNumber)) {
            log.warn("권한 없음: subscriber={}, target={}", subscriberNumber, targetUserNumber);
            throw new AccessDeniedException(
                    String.format("사용자 %s의 위치를 볼 권한이 없습니다.", targetUserNumber)
            );
        }

        log.info("위치 구독 승인: subscriber={}, target={}",
                subscriberNumber, targetUserNumber);

        // 캐시된 최신 위치 반환 (있으면)
        LocationUpdateDto latestLocation = cacheService.getLatestLocation(targetUserNumber);

        if (latestLocation != null) {
            log.debug("캐시된 위치 즉시 전송: target={}, lat={}, lng={}",
                    targetUserNumber, latestLocation.getLatitude(), latestLocation.getLongitude());
        } else {
            log.debug("캐시된 위치 없음: target={}", targetUserNumber);
        }

        return latestLocation;
    }
}
