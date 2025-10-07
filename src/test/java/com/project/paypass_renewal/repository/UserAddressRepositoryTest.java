package com.project.paypass_renewal.repository;

import com.project.paypass_renewal.domain.UserAddress;
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
class UserAddressRepositoryTest {

    @Autowired UserAddressRepository userAddressRepository;

    @Test
    @DisplayName("번호로_유저주소_조회_테스트")
    void findByNumberTest() {
        // given

        UserAddress userAddress = new UserAddress("01012345678", "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");

        // when
        userAddressRepository.save(userAddress);
        UserAddress findUserAddress = userAddressRepository.findByNumber("01012345678");

        // then
        assertThat(findUserAddress).isNotNull();
        assertThat(findUserAddress.getNumber()).isEqualTo("01012345678");

    }

    @Test
    @DisplayName("유저주소_저장_테스트")
    void saveUserAddressTest() {
        // given
        UserAddress userAddress = new UserAddress("01012345678", "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41");

        // when
        UserAddress savedUserAddress = userAddressRepository.save(userAddress);

        // then
        assertThat(savedUserAddress).isNotNull();
        assertThat(savedUserAddress.getNumber()).isEqualTo("01012345678");
        assertThat(savedUserAddress.getHomeStreetAddress()).isEqualTo("서울시 노원구 노원로 564");
    }

    @Test
    @DisplayName("유저번호로_도로명주소_조회_테스트")
    void findHomeStreetAddressByNumberTest() {
        String number = "01012345678";
        String homeStreetAddress = "서울시 노원구 노원로 564";

        // given
        UserAddress userAddress = new UserAddress(number, homeStreetAddress, "1001-102", "서울 노원구 노원로18길 41");
        userAddressRepository.save(userAddress);

        // when
        String findHomeStreetAddress = userAddressRepository.findHomeStreetAddressByNumber(number);

        // then
        assertThat(findHomeStreetAddress).isEqualTo(userAddress.getHomeStreetAddress());

    }

}
