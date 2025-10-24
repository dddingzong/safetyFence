package com.project.safetyFence.service;

import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.UserAddress;
import com.project.safetyFence.domain.dto.request.CenterAddressUpdateRequestDto;
import com.project.safetyFence.domain.dto.request.HomeAddressUpdateRequestDto;
import com.project.safetyFence.domain.dto.request.PasswordUpdateRequestDto;
import com.project.safetyFence.domain.dto.response.SimpleGeofenceResponseDto;
import com.project.safetyFence.domain.dto.response.UserDataResponseDto;
import com.project.safetyFence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void updatePassword(String userNumber, PasswordUpdateRequestDto dto) {
        User user = userRepository.findByNumber(userNumber);

        // 현재 비밀번호 검증
        if (!user.getPassword().equals(dto.getCurrentPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 변경
        user.updatePassword(dto.getNewPassword());
    }

    @Transactional
    public void updateHomeAddress(String userNumber, HomeAddressUpdateRequestDto dto) {
        User user = userRepository.findByNumber(userNumber);
        UserAddress userAddress = user.getUserAddress();

        if (userAddress == null) {
            throw new IllegalArgumentException("사용자 주소 정보가 없습니다.");
        }

        // 집주소 변경
        userAddress.updateHomeStreetAddress(
            dto.getHomeStreetAddress(),
            dto.getHomeStreetAddressDetail()
        );
    }

    @Transactional
    public void updateCenterAddress(String userNumber, CenterAddressUpdateRequestDto dto) {
        User user = userRepository.findByNumber(userNumber);
        UserAddress userAddress = user.getUserAddress();

        if (userAddress == null) {
            throw new IllegalArgumentException("사용자 주소 정보가 없습니다.");
        }

        // 센터주소 변경
        userAddress.updateCenterStreetAddress(dto.getCenterStreetAddress());
    }
}
