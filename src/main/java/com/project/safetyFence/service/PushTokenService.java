package com.project.safetyFence.service;

import com.project.safetyFence.domain.token.PushToken;
import com.project.safetyFence.repository.PushTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushTokenService {

    private final PushTokenRepository pushTokenRepository;

    public void saveOrUpdateToken(String number, String token) {
        PushToken pushToken = pushTokenRepository.findByNumber(number)
                .map(existing -> {
                    existing = PushToken.builder()
                            .id(existing.getId())
                            .number(number)
                            .token(token)
                            .build();
                    return existing;
                })
                .orElse(PushToken.builder()
                        .number(number)
                        .token(token)
                        .build());

        pushTokenRepository.save(pushToken);
    }

    public String getTokenByNumber(String number) {
        return pushTokenRepository.findByNumber(number)
                .map(PushToken::getToken)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 푸시 토큰을 찾을 수 없습니다: " + number));
    }
}