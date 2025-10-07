package com.project.paypass_renewal.service;

import com.project.paypass_renewal.domain.data.Station;
import com.project.paypass_renewal.domain.dto.request.StationNumberRequestDto;
import com.project.paypass_renewal.domain.dto.request.UserPaypassGeofenceRequestDto;
import com.project.paypass_renewal.domain.dto.response.StationListResponseDto;
import com.project.paypass_renewal.repository.StationRepository;
import com.project.paypass_renewal.util.CheckCurrentStationCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;
    private final CheckCurrentStationCondition checkCurrentStationCondition;

    public List<StationListResponseDto> getStationList() {
        List<Station> stationList = stationRepository.findAll();

        return stationList.stream().map(
                station -> new StationListResponseDto(
                        station.getName(),
                        station.getStationNumber(),
                        station.getLongitude(),
                        station.getLatitude()
                )
        ).toList();
    }

    public String findBusInfoByStationNumber(Long stationNumber) {
        return stationRepository.findBusInfoByStationNumber(stationNumber);
    }

    public Station findByStationNumber(Long stationNumber) {
        return stationRepository.findByStationNumber(stationNumber);
    }

    public Map<String, BigDecimal> getStationLatLng(StationNumberRequestDto stationNumberRequestDto) {
        Long stationNumber = stationNumberRequestDto.getStationNumber();
        Station station = stationRepository.findByStationNumber(stationNumber);

        return Map.of("latitude", station.getLatitude(), "longitude", station.getLongitude());
    }

    public boolean checkStationCondition(UserPaypassGeofenceRequestDto userPaypassGeofenceRequestDto) {
        checkCurrentStationCondition.checkStationCondition(userPaypassGeofenceRequestDto);
        return true;
    }

}
