package com.project.safetyFence.controller;

import com.project.safetyFence.domain.Geofence;
import com.project.safetyFence.domain.dto.request.FenceInRequestDto;
import com.project.safetyFence.domain.dto.request.GeofenceDeleteRequestDto;
import com.project.safetyFence.domain.dto.request.GeofenceRequestDto;
import com.project.safetyFence.domain.dto.response.GeofenceResponseDto;
import com.project.safetyFence.service.GeofenceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GeofenceController {

    private final GeofenceService geofenceService;

    @PostMapping("/geofence/list")
    public ResponseEntity<List<GeofenceResponseDto>> findGeofenceList(HttpServletRequest request) {
        String userNumber = (String) request.getAttribute("userNumber");
        List<GeofenceResponseDto> responseDtos = new ArrayList<>();
        List<Geofence> geofences = geofenceService.findGeofenceByNumber(userNumber);

        for (Geofence geofence : geofences) {
            Long id = geofence.getId();
            String name = geofence.getName();
            String address = geofence.getAddress();
            BigDecimal latitude = geofence.getLatitude();
            BigDecimal longitude = geofence.getLongitude();
            int type = geofence.getType();
            LocalDateTime startTime = geofence.getStartTime();
            LocalDateTime endTime = geofence.getEndTime();
            int maxSequence = geofence.getMaxSequence();

            GeofenceResponseDto responseDto = new GeofenceResponseDto(id, name, address, latitude, longitude, type, startTime, endTime, maxSequence);
            responseDtos.add(responseDto);
        }

        return ResponseEntity.ok(responseDtos);
    }

    @PostMapping("/geofence/userFenceIn")
    public ResponseEntity<String> userFenceIn(@RequestBody FenceInRequestDto fenceInRequestDto,
                                             HttpServletRequest request) {
        String userNumber = (String) request.getAttribute("userNumber");
        geofenceService.userFenceIn(userNumber, fenceInRequestDto.getGeofenceId());
        return ResponseEntity.ok("사용자의 진입이 성공적으로 감지되었습니다.");
    }

    @PostMapping("/geofence/newFence")
    public ResponseEntity<String> createNewFence(@RequestBody GeofenceRequestDto geofenceRequestDto,
                                                 HttpServletRequest request) {
        String userNumber = (String) request.getAttribute("userNumber");
        geofenceService.createNewFence(userNumber, geofenceRequestDto);
        return ResponseEntity.ok("새로운 지오펜스가 성공적으로 생성되었습니다.");
    }

    @DeleteMapping("/geofence/deleteFence")
    public ResponseEntity<String> deleteFence(@RequestBody GeofenceDeleteRequestDto geofenceRequestDto
            , HttpServletRequest request) {
        String userNumber = (String) request.getAttribute("userNumber");
        geofenceService.deleteFence(userNumber, geofenceRequestDto.getId());
        return ResponseEntity.ok("지오펜스가 성공적으로 삭제되었습니다.");
    }

}
