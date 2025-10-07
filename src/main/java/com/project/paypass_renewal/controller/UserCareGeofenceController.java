package com.project.paypass_renewal.controller;

import com.project.paypass_renewal.domain.dto.request.CareGeofenceMoveDto;
import com.project.paypass_renewal.domain.dto.request.NumberRequestDto;
import com.project.paypass_renewal.domain.dto.response.UserCareGeofenceResponseDto;
import com.project.paypass_renewal.service.UserCareGeofenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserCareGeofenceController {

    private final UserCareGeofenceService userCareGeofenceService;

    @PostMapping("/geofence/getCareGeofence")
    public ResponseEntity<UserCareGeofenceResponseDto> getCareGeofence(@RequestBody NumberRequestDto numberRequestDto) {
        String number = numberRequestDto.getNumber();

        log.info("사용자 " + number + "의 케어 지오펜스 정보 요청");

        UserCareGeofenceResponseDto userCareGeofenceResponseDto = userCareGeofenceService.findUserCareGeofence(number);

        if (userCareGeofenceResponseDto == null) {
            log.warn("사용자 " + number + "의 케어 지오펜스 정보가 존재하지 않습니다.");
            return ResponseEntity.notFound().build();
        }

        log.info("사용자 " + number + "의 케어 지오펜스 정보 조회 완료");
        return ResponseEntity.ok(userCareGeofenceResponseDto);
    }

    @PostMapping("geofence/careGeofence/algorithm")
    public ResponseEntity<String> checkCareGeofenceAlgorithm(@RequestBody CareGeofenceMoveDto careGeofenceMoveDto) {
        
        String result = userCareGeofenceService.checkCareGeofenceAlgorithm(careGeofenceMoveDto);

        if (result.equals("notValidAlgorithm")) {
            log.info("케어 지오펜스 시간 알고리즘 불만족으로 로그 미저장");
            return ResponseEntity.ok(result);
        }
        
        return ResponseEntity.ok(result);
    }



}
