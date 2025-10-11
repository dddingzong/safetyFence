package com.project.safetyFence.service;

import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.UserAddress;
import com.project.safetyFence.domain.dto.request.CenterAddressRequestDto;
import com.project.safetyFence.domain.dto.request.HomeAddressRequestDto;
import com.project.safetyFence.domain.dto.request.NumberRequestDto;
import com.project.safetyFence.domain.dto.response.MyPageResponseDto;
import com.project.safetyFence.repository.UserAddressRepository;
import com.project.safetyFence.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;

    public MyPageResponseDto getMyPageData(NumberRequestDto numberRequestDto) {

        String number = numberRequestDto.getNumber();

        User user = userRepository.findByNumber(number);
        UserAddress userAddress = userAddressRepository.findByNumber(number);

        MyPageResponseDto myPageResponseDto = userAndAddressToEntity(user, userAddress);

        return myPageResponseDto;
    }

    private MyPageResponseDto userAndAddressToEntity(User user, UserAddress userAddress) {
        return new MyPageResponseDto(
                user.getName(),
                user.getNumber(),
                userAddress.getHomeStreetAddress(),
                userAddress.getHomeStreetAddressDetail(),
                userAddress.getCenterStreetAddress(),
                user.getLinkCode()
        );
    }

    // 사용자 집 주소 변경. 변경사항: homeAddress, homeStreetAddress, homeStreetAddressDetail
    @Transactional
    public String updateHomeAddress(HomeAddressRequestDto homeAddressRequestDto) {
        String number = homeAddressRequestDto.getNumber();
        String homeAddress = homeAddressRequestDto.getHomeAddress();
        String homeStreetAddress = homeAddressRequestDto.getHomeStreetAddress();
        String homeStreetAddressDetail = homeAddressRequestDto.getHomeStreetAddressDetail();

        User user = userRepository.findByNumber(number);
        user.updateHomeAddress(homeAddress);
        UserAddress userAddress = userAddressRepository.findByNumber(number);
        userAddress.updateHomeStreetAddress(homeStreetAddress, homeStreetAddressDetail);

        return number;
    }


    // 사용자 센터 주소 변경 변경사항: centerAddress, centerStreetAddress
    @Transactional
    public String updateCenterAddress(CenterAddressRequestDto centerAddressRequestDto){
        String number = centerAddressRequestDto.getNumber();
        String centerAddress = centerAddressRequestDto.getCenterAddress();
        String centerStreetAddress = centerAddressRequestDto.getCenterStreetAddress();

        User user = userRepository.findByNumber(number);
        user.updateCenterAddress(centerAddress);

        UserAddress userAddress = userAddressRepository.findByNumber(number);
        userAddress.updateCenterStreetAddress(centerStreetAddress);

        return number;
    }
}
