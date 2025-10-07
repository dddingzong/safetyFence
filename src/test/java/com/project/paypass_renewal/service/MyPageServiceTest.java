package com.project.paypass_renewal.service;

import com.project.paypass_renewal.domain.UserAddress;
import com.project.paypass_renewal.domain.dto.request.NumberRequestDto;
import com.project.paypass_renewal.domain.dto.response.MyPageResponseDto;
import com.project.paypass_renewal.repository.UserAddressRepository;
import com.project.paypass_renewal.repository.UserRepository;
import com.project.paypass_renewal.support.UserTestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserAddressRepository userAddressRepository;

    @InjectMocks
    MyPageService myPageService;

    @Test
    @DisplayName("마이페이지_데이터_조회_테스트")
    void getMyPageDataTest() {
        // 마이페이지 로딩 시 데이터 조회 메서드 테스트
        // 필요 데이터: name, number, homeStreetAddress, homeStreetAddressDetail, centerStreetAddress, LinkCode

        // given
        NumberRequestDto numberRequestDto = new NumberRequestDto("01012345678");

        // stub
        when(userRepository.findByNumber(any(String.class))).thenReturn(UserTestUtils.createDummyUser());
        when(userAddressRepository.findByNumber(any(String.class))).thenReturn(
                new UserAddress("01012345678", "서울시 노원구 노원로 564", "1001-102", "서울 노원구 노원로18길 41"));

        // when
        MyPageResponseDto myPageResponseDto = myPageService.getMyPageData(numberRequestDto);

        // then
        Assertions.assertThat(myPageResponseDto).isNotNull();
        Assertions.assertThat(myPageResponseDto.getName()).isEqualTo("더미유저");
        Assertions.assertThat(myPageResponseDto.getNumber()).isEqualTo("01012345678");
        Assertions.assertThat(myPageResponseDto.getHomeStreetAddress()).isEqualTo("서울시 노원구 노원로 564");
        Assertions.assertThat(myPageResponseDto.getHomeStreetAddressDetail()).isEqualTo("1001-102");
        Assertions.assertThat(myPageResponseDto.getCenterStreetAddress()).isEqualTo("서울 노원구 노원로18길 41");
        Assertions.assertThat(myPageResponseDto.getLinkCode()).isEqualTo("123456");

    }

}
