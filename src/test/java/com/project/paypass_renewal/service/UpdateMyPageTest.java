package com.project.paypass_renewal.service;

import com.project.paypass_renewal.domain.User;
import com.project.paypass_renewal.domain.UserAddress;
import com.project.paypass_renewal.domain.dto.request.CenterAddressRequestDto;
import com.project.paypass_renewal.domain.dto.request.HomeAddressRequestDto;
import com.project.paypass_renewal.repository.UserAddressRepository;
import com.project.paypass_renewal.repository.UserRepository;
import com.project.paypass_renewal.support.UserTestUtils;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UpdateMyPageTest {

    @Autowired
    MyPageService myPageService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserAddressRepository userAddressRepository;

    @Test
    @DisplayName("유저_집주소_변경_테스트")
    void updateHomeAddressTest() {
        // 변경사항: homeAddress, homeStreetAddress, homeStreetAddressDetail
        // given
        String number = "01012345678";

        HomeAddressRequestDto homeAddressRequestDto = new HomeAddressRequestDto(number,"01849","서울특별시 노원구 공릉로 95","102");

        User user = UserTestUtils.createDummyUser();
        UserAddress userAddress = new UserAddress(number, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");

        userRepository.save(user);
        userAddressRepository.save(userAddress);

        // when
        myPageService.updateHomeAddress(homeAddressRequestDto);

        // then
        assertThat(userRepository.findByNumber(number).getHomeAddress()).isEqualTo(homeAddressRequestDto.getHomeAddress());
        assertThat(userAddressRepository.findByNumber(number).getHomeStreetAddress()).isEqualTo(homeAddressRequestDto.getHomeStreetAddress());
        assertThat(userAddressRepository.findByNumber(number).getHomeStreetAddressDetail()).isEqualTo(homeAddressRequestDto.getHomeStreetAddressDetail());
    }

    @Test
    @DisplayName("유저_센터주소_변경_테스트")
    void updateCenterAddressTest() {
        // 변경사항: centerAddress, centerStreetAddress
        // given
        String number = "01012345678";

        CenterAddressRequestDto centerAddressRequestDto = new CenterAddressRequestDto(number, "12315", "서울시 변경 후 테스트 데이터");

        User user = UserTestUtils.createDummyUser();
        UserAddress userAddress = new UserAddress(number, "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");

        userRepository.save(user);
        userAddressRepository.save(userAddress);

        // when
        myPageService.updateCenterAddress(centerAddressRequestDto);

        // then
        assertThat(userRepository.findByNumber(number).getCenterAddress()).isEqualTo(centerAddressRequestDto.getCenterAddress());
        assertThat(userAddressRepository.findByNumber(number).getCenterStreetAddress()).isEqualTo(centerAddressRequestDto.getCenterStreetAddress());
    }
}
