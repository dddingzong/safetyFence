package com.project.paypass_renewal.controller;


import com.project.paypass_renewal.domain.dto.request.StationNumberRequestDto;
import com.project.paypass_renewal.domain.dto.response.StationListResponseDto;
import com.project.paypass_renewal.service.StationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @GetMapping("/station/getStationList")
    public ResponseEntity<List<StationListResponseDto>> getStationList() {
        List<StationListResponseDto> stationList = stationService.getStationList();
        return ResponseEntity.ok(stationList);
    }

    // stationNumber를 매개변수로 받고 위도 경도 반환 메서드 작성
    @PostMapping("/station/getStationLatLng")
    public ResponseEntity<Map<String, BigDecimal>> getStationLatLng(@RequestBody StationNumberRequestDto stationNumberRequestDto) {
        Map<String, BigDecimal> latLngMap = stationService.getStationLatLng(stationNumberRequestDto);
        log.info("stationNumber 활용하여 위도 경도 조회: " + latLngMap);
        return ResponseEntity.ok(latLngMap);
    }



}
