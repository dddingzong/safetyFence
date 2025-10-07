package com.project.paypass_renewal.service;

import com.project.paypass_renewal.domain.UserAddress;
import com.project.paypass_renewal.domain.dto.request.UserRequestDto;
import com.project.paypass_renewal.repository.UserAddressRepository;
import com.project.paypass_renewal.support.UserDtoTestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

    @Mock
    UserAddressRepository userAddressRepository;

    @InjectMocks
    UserAddressService userAddressService;

    @Test
    @DisplayName("유저주소_저장_테스트")
    void saveNewUserAddressTest() {
        // given
        UserRequestDto userRequestDto = UserDtoTestUtil.createDummyUserDto();

        // when
        UserAddress savedUserAddress = userAddressService.saveNewUserAddress(userRequestDto);

        // then
        assertThat(savedUserAddress).isNotNull();
        assertThat(savedUserAddress.getNumber()).isEqualTo(userRequestDto.getNumber());
        assertThat(savedUserAddress.getHomeStreetAddress()).isEqualTo(userRequestDto.getHomeStreetAddress());
        assertThat(savedUserAddress.getCenterStreetAddress()).isEqualTo(userRequestDto.getCenterStreetAddress());
    }

}
