package com.project.paypass_renewal.controller;

import com.project.paypass_renewal.service.PushTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PushTokenController {

    private final PushTokenService pushTokenService;

    @PostMapping("/user/savePushToken")
    public ResponseEntity<String> savePushToken(@RequestBody Map<String, String> request) {
        String number = request.get("number");
        String token = request.get("token");

        if (number == null || token == null) {
            return ResponseEntity.badRequest().body("number와 token은 필수입니다.");
        }

        pushTokenService.saveOrUpdateToken(number, token);
        return ResponseEntity.ok("푸시 토큰 저장 완료");
    }

}
