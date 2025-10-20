package com.project.safetyFence.service;

import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.UserAddress;
import com.project.safetyFence.domain.dto.request.SignUpRequestDto;
import com.project.safetyFence.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;

    public UserAddress makeUserAddressEntity(SignUpRequestDto signUpRequestDto, User user) {
        // userAddress 저장
        String homeAddress = signUpRequestDto.getHomeAddress();
        String centerAddress = signUpRequestDto.getCenterAddress();
        String homeStreetAddress = signUpRequestDto.getHomeStreetAddress();
        String homeStreetAddressDetail = signUpRequestDto.getHomeStreetAddressDetail();
        String centerStreetAddress = signUpRequestDto.getCenterStreetAddress();

        UserAddress userAddress = new UserAddress(user, homeAddress, centerAddress, homeStreetAddress, homeStreetAddressDetail, centerStreetAddress);

        return userAddress;
    }

    @Transactional
    public void saveUserAddress(UserAddress userAddress) {
        userAddressRepository.save(userAddress);
    }
}
