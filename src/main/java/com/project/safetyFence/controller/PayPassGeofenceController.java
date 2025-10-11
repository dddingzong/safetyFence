package com.project.safetyFence.controller;

import com.project.safetyFence.domain.PaypassGeofence;
import com.project.safetyFence.domain.UserLocation;
import com.project.safetyFence.domain.dto.request.NumberRequestDto;
import com.project.safetyFence.domain.dto.request.UserPaypassGeofenceRequestDto;
import com.project.safetyFence.domain.dto.response.UserLatLngListResponseDto;
import com.project.safetyFence.service.PaypassGeofenceService;
import com.project.safetyFence.service.StationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequiredArgsConstructor
public class PayPassGeofenceController {

    private final StationService stationService;
    private final PaypassGeofenceService paypassGeofenceService;

    @PostMapping("/geofence/userFenceIn")
    public ResponseEntity<String> userFenceIn(@RequestBody UserPaypassGeofenceRequestDto userPaypassGeofenceRequestDto) {
        log.info("****************************************************************");
        log.info("사용자가 geofence에 접근했기 때문에 userGeofenceIn method를 실행합니다.");
        log.info("****************************************************************");

        if (!stationService.checkStationCondition(userPaypassGeofenceRequestDto)) {
            return ResponseEntity.ok("fail to save geofence data: station condition not met");
        }

        String number = userPaypassGeofenceRequestDto.getNumber();
        Long stationNumber = userPaypassGeofenceRequestDto.getStationNumber();

        // statationNumber로 busInfo 가져오기
        String busInfo = stationService.findBusInfoByStationNumber(stationNumber);

        // geofenceLocation entity 생성
        paypassGeofenceService.createGeofenceLocation(number, stationNumber, busInfo);

        return ResponseEntity.ok("success save geofence data");
    }

    @PostMapping("/geofence/userFenceOut")
    public ResponseEntity<String> userFenceOut(@RequestBody UserPaypassGeofenceRequestDto userPaypassGeofenceRequestDto) {
        log.info("****************************************************************");
        log.info("사용자가 geofence에서 이탈했기 때문에 userGeofenceOut method를 실행합니다.");
        log.info("****************************************************************");

        String number = userPaypassGeofenceRequestDto.getNumber();
        Long stationNumber = userPaypassGeofenceRequestDto.getStationNumber();

        // number와 stationNumber를 활용해서 해당 entity 가져오기
        List<PaypassGeofence> paypassGeofences = paypassGeofenceService.findByNumberAndStationNumber(number, stationNumber);

        // geofenceList의 조건에 따라서 fenceOutTime 업데이트
        // entity가 존재하지 않으면 2분 전 fenceIn을 한다고 가정한다.
        if (paypassGeofences.isEmpty()){
            updateFenceOutTimeNoEntity(number, stationNumber);
        }

        // entity가 하나라면 해당 entity의 fenceOutTime 추가
        if (paypassGeofences.size() == 1){
            updateFenceOutTimeOneEntity(paypassGeofences);
        }

        // 조회하였을 때 두개 이상인 경우에는 userFenceOut이 null 값인 엔티티 찾기
        if (paypassGeofences.size() > 1){
            updateFenceOutTimeEntities(paypassGeofences);
        }

        return ResponseEntity.ok("success update fenceOutTime");
    }

    @PostMapping("/geofence/getUserMovingHistory")
    public  ResponseEntity<List<UserLatLngListResponseDto>> getUserMovingHistory(@RequestBody NumberRequestDto numberRequestDto) {
        List<UserLocation> userLocationList = paypassGeofenceService.getUserMovingHistory(numberRequestDto);
        List<UserLatLngListResponseDto> historyList = userLocationList.stream()
                .map(location -> new UserLatLngListResponseDto(location.getLatitude(), location.getLongitude()))
                .collect(Collectors.toList());

        log.info("사용자 이동에 따른 경로를 지도에 표시합니다.");

        return ResponseEntity.ok(historyList);
    }

    private void updateFenceOutTimeNoEntity(String number, Long stationNumber){
        log.info("stationNumber에 부합하는 entity가 존재하지 않기 때문에 2분 전 fenceIn을 가정하고 entity를 생성합니다.");

        // stationNumber 활용해서 busInfo 가져오기
        String busInfo = stationService.findBusInfoByStationNumber(stationNumber);

        // paypassGeofence entity 생성
        PaypassGeofence paypassGeofence = paypassGeofenceService.userFenceOutWithoutEntity(number, stationNumber, busInfo, LocalDateTime.now());

        paypassGeofenceService.save(paypassGeofence);
        log.info("paypassGeofence 데이터를 생성 후 저장했습니다. (entity 없이 fenceOut한 경우)");
        log.info("paypassGeofence: {}", paypassGeofence);
    }

    private void updateFenceOutTimeOneEntity(List<PaypassGeofence> paypassGeofences){
        PaypassGeofence paypassGeofence = paypassGeofences.get(0);
        paypassGeofence.userFenceOut();
        paypassGeofenceService.save(paypassGeofence);
        log.info("하나의 entity를 발견하여 fenceOutTime을 추가합니다.");
    }

    private void updateFenceOutTimeEntities(List<PaypassGeofence> paypassGeofences) {
        paypassGeofences.stream()
                .filter(paypassGeofenceService::fenceOutTimeIsNull)
                .findFirst()
                .ifPresent(paypassGeofenceService::userFenceOut);
        log.info("여러 개의 entity 중 첫 번째 null fenceOutTime을 업데이트했습니다.");
    }


}
