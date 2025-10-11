package com.project.safetyFence.service;

import com.project.safetyFence.domain.DetailLog;
import com.project.safetyFence.domain.Log;
import com.project.safetyFence.domain.PaypassGeofence;
import com.project.safetyFence.domain.data.Station;
import com.project.safetyFence.domain.dto.request.NumberRequestDto;
import com.project.safetyFence.domain.dto.response.LogListResponseDto;
import com.project.safetyFence.repository.LogRepository;
import com.project.safetyFence.repository.UserRepository;
import com.project.safetyFence.util.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    private final UserRepository userRepository;
    private final StationService stationService;
    private final DetailLogService detailLogService;

    private final PushNotificationService pushNotificationService;

    public List<LogListResponseDto> getLogListByNumber(NumberRequestDto numberRequestDto) {
        List<LogListResponseDto> logListResponseDtoList = new ArrayList<>();

        String number = numberRequestDto.getNumber();

        List<Log> logList = logRepository.findByNumberOrderByDepartureTimeDesc(number);

        for (Log log : logList) {
            LogListResponseDto logListResponseDto = logToLogListResponseDto(log);
            logListResponseDtoList.add(logListResponseDto);
        }
        
        return logListResponseDtoList;
    }

    private LogListResponseDto logToLogListResponseDto(Log log) {
        Long id = log.getId();
        String number = log.getNumber();
        LocalDateTime departureTime = log.getDepartureTime();
        LocalDateTime arrivalTime = log.getArrivalTime();
        String departureLocation = log.getDepartureLocation();
        String arrivalLocation = log.getArrivalLocation();

        String name = userRepository.findByNumber(number).getName();

        return new LogListResponseDto(id ,number, name, departureTime, arrivalTime, departureLocation, arrivalLocation);
    }

    public void saveLogData(String number, Map<List<PaypassGeofence>, List<String>> resultMap){
        log.info("resultMap 확인: " + resultMap);

        for (var geofenceLocationListAndRouteIdList : resultMap.entrySet()) {
            List<PaypassGeofence> geofenceLocationList = geofenceLocationListAndRouteIdList.getKey();
            List<String> routeIdList = geofenceLocationListAndRouteIdList.getValue();
            // log save
            Log logData = saveLog(number, geofenceLocationList, routeIdList);

            // routeIdList String으로 변환
            String routeIdString = String.join(",", routeIdList);

            // detailLog save
            detailLogService.saveDetailLogData(number, logData, geofenceLocationList, routeIdString);
        }

        pushNotificationService.sendNotificationToUser(number, "로그 저장 완료", "귀하의 이동경로가 성공적으로 저장되었습니다. 출발지와 도착지를 확인하세요.");
    }

    private Log saveLog(String number, List<PaypassGeofence> geofenceLocationList, List<String> routeIdList) {
        // 출발 정류장 정보
        PaypassGeofence departure = geofenceLocationList.get(0);
        LocalDateTime departureTime = departure.getFenceOutTime();
        Long departureStationNumber = departure.getStationNumber();
        Station departureStation = stationService.findByStationNumber(departureStationNumber);
        String departureLocation = departureStation.getName();


        // 도착 정류장 정보
        PaypassGeofence arrival = geofenceLocationList.get(geofenceLocationList.size() - 1);
        LocalDateTime arrivalTime = arrival.getFenceInTime();
        Long arrivalStationNumber = arrival.getStationNumber();
        Station arrivalStation = stationService.findByStationNumber(arrivalStationNumber);
        String arrivalLocation = arrivalStation.getName();

        Log logData = new Log(number, departureTime, arrivalTime, departureLocation, arrivalLocation);

        logRepository.save(logData);
        log.info("log data 저장했습니다." + logData);

        return logData;
    }


    public void saveLog(Log log) {
        logRepository.save(log);
    }

}
