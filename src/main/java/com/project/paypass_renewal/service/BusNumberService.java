package com.project.paypass_renewal.service;

import com.project.paypass_renewal.domain.data.BusNumber;
import com.project.paypass_renewal.repository.BusNumberRepository;
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
