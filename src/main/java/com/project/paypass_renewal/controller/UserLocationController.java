package com.project.paypass_renewal.controller;


import com.project.paypass_renewal.domain.dto.request.DetailLogPathRequestDto;
import com.project.paypass_renewal.domain.dto.request.NumberRequestDto;
import com.project.paypass_renewal.domain.dto.request.UserLocationRequestDto;
import com.project.paypass_renewal.domain.dto.response.UserLocationResponseDto;
import com.project.paypass_renewal.service.UserLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserLocationController {

    private final  UserLocationService userLocationService;

    @PostMapping("/user/saveUserLocation")
    public ResponseEntity<String> saveNewUser(@RequestBody UserLocationRequestDto userLocationRequestDto){
        userLocationService.saveUserLocation(userLocationRequestDto);
        log.info("userLocation 위치 저장 완료");
        return ResponseEntity.ok("saveSuccess");
    }

    @PostMapping("/user/getRecentUserLocation")
    public ResponseEntity<UserLocationResponseDto> getRecentUserLocation(@RequestBody NumberRequestDto numberRequestDto) {
        UserLocationResponseDto userLocationResponseDto = userLocationService.findRecentLocationByNumber(numberRequestDto);

        return ResponseEntity.ok(userLocationResponseDto);
    }

    @PostMapping("/user/getUserLocationList")
    public ResponseEntity<List<Map<String, BigDecimal>>> getUserMovingPath(@RequestBody DetailLogPathRequestDto detailLogPathRequestDto) {
        log.info("사용자 세부로그 이동 경로를 조회합니다");
        
        List<Map<String, BigDecimal>> latLngList = userLocationService.getUserMovingPath(detailLogPathRequestDto);

        return ResponseEntity.ok(latLngList);
    }




}
