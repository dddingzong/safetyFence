package com.project.safetyFence.service;

import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.UserAddress;
import com.project.safetyFence.domain.dto.response.SimpleGeofenceResponseDto;
import com.project.safetyFence.domain.dto.response.UserDataResponseDto;
import com.project.safetyFence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;

    public UserDataResponseDto getUserData(String number) {
        User user = userRepository.findByNumber(number);
        String name = user.getName();
        LocalDate birth = user.getBirth();
        String linkCode = user.getLinkCode();

        UserAddress userAddress = user.getUserAddress();
        String homeStreetAddress = userAddress.getHomeStreetAddress();
        String homeStreetAddressDetail = userAddress.getHomeStreetAddressDetail();
        String centerStreetAddress = userAddress.getCenterStreetAddress();

        List<SimpleGeofenceResponseDto> geofenceDtos = user.getGeofences().stream()
                .map(geofence -> new SimpleGeofenceResponseDto(
                        geofence.getId(),
                        geofence.getName(),
                        geofence.getAddress(),
                        geofence.getType(),
                        geofence.getStartTime(),
                        geofence.getEndTime(
                ))).toList();

        UserDataResponseDto userDataResponseDto = new UserDataResponseDto(
                name,
                birth.toString(),
                homeStreetAddress + " " + homeStreetAddressDetail,
                centerStreetAddress,
                linkCode,
                geofenceDtos
        );

        return userDataResponseDto;
    }
}
