package com.project.paypass_renewal.service;

import com.project.paypass_renewal.domain.DetailLog;
import com.project.paypass_renewal.domain.Log;
import com.project.paypass_renewal.domain.PaypassGeofence;
import com.project.paypass_renewal.domain.dto.request.LogIdRequestDto;
import com.project.paypass_renewal.repository.DetailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DetailLogService {

    private final DetailLogRepository detailLogRepository;

    public List<DetailLog> findDetailLogsByLogId(LogIdRequestDto logIdRequestDto) {
        Long logId = logIdRequestDto.getLogId();
        return detailLogRepository.findByLogId(logId);
    }

    public void saveDetailLogData(String number, Log logData, List<PaypassGeofence> geofenceLocationList, String routeIdString) {
        for (PaypassGeofence geofenceLocation : geofenceLocationList) {
            Long logId = logData.getId();
            LocalDateTime fenceInTime = geofenceLocation.getFenceInTime();
            LocalDateTime fenceOutTime = geofenceLocation.getFenceOutTime();
            Long stationNumber = geofenceLocation.getStationNumber();

            DetailLog detailLog = new DetailLog(logId, number, fenceInTime, fenceOutTime, stationNumber, routeIdString);

            detailLogRepository.save(detailLog);

            log.info("detailLog data 저장했습니다." + detailLog);
        }
    }


}
