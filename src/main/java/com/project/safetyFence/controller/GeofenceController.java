package com.project.safetyFence.controller;

import com.project.safetyFence.domain.Geofence;
import com.project.safetyFence.domain.dto.request.FenceInRequestDto;
import com.project.safetyFence.domain.dto.request.GeofenceRequestDto;
import com.project.safetyFence.domain.dto.request.NumberRequestDto;
import com.project.safetyFence.domain.dto.response.GeofenceResponseDto;
import com.project.safetyFence.service.GeofenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GeofenceController {

    private final GeofenceService geofenceService;

    @PostMapping("/geofence/list")
    public ResponseEntity<List<GeofenceResponseDto>> findGeofenceList(@RequestBody NumberRequestDto requestDto) {
        List<GeofenceResponseDto> responseDtos = new ArrayList<>();
        List<Geofence> geofences = geofenceService.findGeofenceByNumber(requestDto);

        for (Geofence geofence : geofences) {
            Long id = geofence.getId();
            String name = geofence.getName();
            String address = geofence.getAddress();
            java.math.BigDecimal latitude = geofence.getLatitude();
            java.math.BigDecimal longitude = geofence.getLongitude();
            int type = geofence.getType();
            java.time.LocalDateTime startTime = geofence.getStartTime();
            java.time.LocalDateTime endTime = geofence.getEndTime();
            int maxSequence = geofence.getMaxSequence();

            GeofenceResponseDto responseDto = new GeofenceResponseDto(id, name, address, latitude, longitude, type, startTime, endTime, maxSequence);
            responseDtos.add(responseDto);
        }

        return ResponseEntity.ok(responseDtos);
    }

    @PostMapping("/geofence/userFenceIn")
    public ResponseEntity<String> userFenceIn(@RequestBody FenceInRequestDto fenceInRequestDto) {
        geofenceService.userFenceIn(fenceInRequestDto);
        return ResponseEntity.ok("사용자의 진입이 성공적으로 감지되었습니다.");
    }

    @PostMapping("geofence/newFence")
    public ResponseEntity<String> createNewFence(@RequestBody GeofenceRequestDto geofenceRequestDto) {
        geofenceService.createNewFence(geofenceRequestDto);
        return ResponseEntity.ok("새로운 지오펜스가 성공적으로 생성되었습니다.");
    }

}
