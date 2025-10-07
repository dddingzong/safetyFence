package com.project.paypass_renewal.service;

import com.project.paypass_renewal.domain.UserAddress;
import com.project.paypass_renewal.domain.dto.request.UserRequestDto;
import com.project.paypass_renewal.repository.UserAddressRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;

    @Transactional
    public UserAddress saveNewUserAddress(UserRequestDto userRequestDto) {
        UserAddress userAddress = toEntity(userRequestDto);
        userAddressRepository.save(userAddress);

        return userAddress;
    }

    private UserAddress toEntity(UserRequestDto userRequestDto) {
        String number = userRequestDto.getNumber();
        String homeStreetAddress = userRequestDto.getHomeStreetAddress();
        String homeStreetAddressDetail = userRequestDto.getHomeStreetAddressDetail();
        String centerStreetAddress = userRequestDto.getCenterStreetAddress();

        return new UserAddress(number, homeStreetAddress, homeStreetAddressDetail, centerStreetAddress);
    }

}
