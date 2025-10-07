package com.project.paypass_renewal.service;

import com.project.paypass_renewal.domain.PaypassGeofence;
import com.project.paypass_renewal.domain.UserLocation;
import com.project.paypass_renewal.domain.dto.request.NumberRequestDto;
import com.project.paypass_renewal.repository.PayPassGeofenceRepository;
import com.project.paypass_renewal.repository.UserLocationRepository;
import com.project.paypass_renewal.util.algorithm.PaypassAverageTimeAlgorithm;
import com.project.paypass_renewal.util.algorithm.PaypassDeleteDuplicateAlgorithm;
import com.project.paypass_renewal.util.algorithm.PaypassSequenceAlgorithm;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaypassGeofenceService {

    private final PayPassGeofenceRepository payPassGeofenceRepository;
    private final UserLocationRepository userLocationRepository;

    private final LogService logService;

    private final PaypassSequenceAlgorithm paypassSequenceAlgorithm;
    private final PaypassAverageTimeAlgorithm paypassAverageTimeAlgorithm;
    private final PaypassDeleteDuplicateAlgorithm paypassDeleteDuplicateAlgorithm;


    @Transactional
    @Scheduled(fixedRate = 60000) // 1분마다 실행 (단위: ms)
    public void algorithmTrigger(){
        log.info("algorithmTrigger가 작동합니다.");
        // 모든 유저에 대한 가장 최근 데이터를 가져오기
        List<PaypassGeofence> recentGeofenceLocations = payPassGeofenceRepository.findMostRecentGeofenceLocationByFenceInTime();
        // fenceInTime이 현재 시간과 30분 차이나는지 확인
        LocalDateTime standardTime = LocalDateTime.now().minusMinutes(30);
        log.info("현재 시간에 30분을 차감하겠습니다. 차감 시간: " + standardTime);

        for (PaypassGeofence geofenceLocation : recentGeofenceLocations) {
            LocalDateTime userFenceInTime = geofenceLocation.getFenceInTime();
            // 30분 차이나면 메인 로직 실행
            if (standardTime.isAfter(userFenceInTime)){
                String number = geofenceLocation.getNumber();
                startUserAlgorithm(number);
            }
        }
    }

    @Transactional
    public void startUserAlgorithm(String number){
        log.info("메인 알고리즘을 실행합니다. number = " + number);
        Map<List<PaypassGeofence>, List<String>> resultMap = startPaypassAlgorithm(number);

        if (resultMap.isEmpty()) {
            return;
        }

        // 이후 데이터 저장
        logService.saveLogData(number, resultMap);

        // geofenceLocation 데이터 삭제
        deleteGeofenceLocations(number);
        log.info(number + "의 메인 알고리즘 실행 성공");
    }

    @Transactional
    private Map<List<PaypassGeofence>, List<String>> startPaypassAlgorithm(String number) {
        // number로 paypassGeofence 조회
        List<PaypassGeofence> geofenceList = payPassGeofenceRepository.findByNumber(number);

        Map<String, List<Long>> sequenceGeofenceMap = paypassSequenceAlgorithm.algorithmStart(geofenceList);
        Map<String, List<Long>> averageTimeGeofenceMap = paypassAverageTimeAlgorithm.algorithmStart(sequenceGeofenceMap, geofenceList);
        Map<List<PaypassGeofence>, List<String>> deleteDuplicateGeofenceMap = paypassDeleteDuplicateAlgorithm.algorithmStart(averageTimeGeofenceMap, geofenceList);

        return deleteDuplicateGeofenceMap;
    }

    @Transactional
    private void deleteGeofenceLocations(String number){
        payPassGeofenceRepository.deleteByNumber(number);
        log.info(number + "의 geofenceLocation 데이터 삭제 성공");
    }

    @Transactional
    public PaypassGeofence createGeofenceLocation(String number, Long stationNumber, String busInfo) {
        PaypassGeofence paypassGeofence = new PaypassGeofence(number, stationNumber, busInfo);
        payPassGeofenceRepository.save(paypassGeofence);

        return paypassGeofence;
    }

    public List<PaypassGeofence> findByNumberAndStationNumber(String number, Long stationNumber) {
        return payPassGeofenceRepository.findByNumberAndStationNumber(number, stationNumber);

    }

    public PaypassGeofence userFenceOutWithoutEntity(String number, Long stationNumber, String busInfo, LocalDateTime userFenceOut) {
        return new PaypassGeofence(number, stationNumber, busInfo, userFenceOut);
    }

    @Transactional
    public PaypassGeofence save(PaypassGeofence paypassGeofence) {
        return payPassGeofenceRepository.save(paypassGeofence);
    }

    @Transactional
    public void userFenceOut(PaypassGeofence paypassGeofence) {
        paypassGeofence.userFenceOut();
    }

    @Transactional
    public boolean fenceOutTimeIsNull(PaypassGeofence paypassGeofence){
        return paypassGeofence.fenceOutTimeIsNull();
    }

    public List<UserLocation> getUserMovingHistory(NumberRequestDto numberRequestDto) {
        String number = numberRequestDto.getNumber();

        List<PaypassGeofence> geofenceList = payPassGeofenceRepository.findByNumber(number);
        log.info("geofenceList = " + geofenceList);

        if (geofenceList.isEmpty()) return new ArrayList<>();

        // fenceInTime 최소, fenceOutTime 최대 구하기
        LocalDateTime minInTime = geofenceList.stream()
                .map(PaypassGeofence::getFenceInTime)
                .min(LocalDateTime::compareTo)
                .orElseThrow()
                .withNano(0);

        LocalDateTime maxOutTime = LocalDateTime.now().withNano(0);

        return userLocationRepository.findByNumberAndSavedTimeBetweenOrderBySavedTime(number, minInTime, maxOutTime);
    }


}
