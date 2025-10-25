package com.project.safetyFence.listener;

import com.project.safetyFence.service.LocationCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

/**
 * WebSocket 이벤트 리스너
 * 연결/해제 시 로그 기록 및 리소스 정리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final LocationCacheService cacheService;

    /**
     * WebSocket 연결 이벤트
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        if (sessionAttributes != null) {
            String userNumber = (String) sessionAttributes.get("userNumber");
            log.info("WebSocket 연결됨: sessionId={}, userNumber={}", sessionId, userNumber);
        } else {
            log.info("WebSocket 연결됨: sessionId={}", sessionId);
        }
    }

    /**
     * WebSocket 연결 해제 이벤트
     * 캐시에서 사용자 위치 삭제
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        if (sessionAttributes != null) {
            String userNumber = (String) sessionAttributes.get("userNumber");

            if (userNumber != null) {
                // 캐시에서 위치 정보 삭제
                cacheService.removeLocation(userNumber);
                log.info("WebSocket 연결 해제: sessionId={}, userNumber={}, 캐시 삭제 완료",
                        sessionId, userNumber);
            } else {
                log.info("WebSocket 연결 해제: sessionId={}", sessionId);
            }
        } else {
            log.info("WebSocket 연결 해제: sessionId={}", sessionId);
        }
    }
}
