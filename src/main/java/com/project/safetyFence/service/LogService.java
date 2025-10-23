package com.project.safetyFence.service;

import com.project.safetyFence.domain.Log;
import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.dto.request.NumberRequestDto;
import com.project.safetyFence.domain.dto.response.LogResponseDto;
import com.project.safetyFence.repository.LogRepository;
import com.project.safetyFence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    private final UserRepository userRepository;

    public List<LogResponseDto> getLogsByUserNumber(String number) {
        List<LogResponseDto> responseDtos = new ArrayList<>();

        User user = userRepository.findByNumber(number);

        List<Log> logs = logRepository.findByUser(user);

        for (Log log : logs) {
            Long id = log.getId();
            String location = log.getLocation();
            String locationAddress = log.getLocationAddress();
            String arriveTime = log.getArriveTime().toString();

            LogResponseDto responseDto = new LogResponseDto(id, location, locationAddress, arriveTime);
            responseDtos.add(responseDto);
        }

        return responseDtos;
    }
}
