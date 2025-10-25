package com.project.safetyFence.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // WebSocket 연결 시 사용자 번호 추출
            String userNumber = accessor.getFirstNativeHeader("userNumber");

            if (userNumber == null || userNumber.isBlank()) {
                log.warn("WebSocket 연결 시도 실패: userNumber가 없습니다.");
                throw new IllegalArgumentException("userNumber는 필수입니다.");
            }

            // 세션 속성에 사용자 번호 저장
            accessor.getSessionAttributes().put("userNumber", userNumber);

            log.info("WebSocket 연결 성공: userNumber={}, sessionId={}",
                    userNumber, accessor.getSessionId());
        }

        return message;
    }
}
