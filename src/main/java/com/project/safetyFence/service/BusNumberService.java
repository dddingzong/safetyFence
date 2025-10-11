package com.project.safetyFence.service;

import com.project.safetyFence.domain.data.BusNumber;
import com.project.safetyFence.repository.BusNumberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BusNumberService {

    private final BusNumberRepository busNumberRepository;

    public String findBusNameByRouteId(String routeId) {
        BusNumber busNumber = busNumberRepository.findByRouteId(routeId);
        String busName = busNumber.getBusName();

        return busName;
    }

}
