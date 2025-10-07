package com.project.paypass_renewal.service;

import com.project.paypass_renewal.domain.User;
import com.project.paypass_renewal.domain.dto.request.LoginRequestDto;
import com.project.paypass_renewal.domain.dto.request.UserRequestDto;
import com.project.paypass_renewal.generator.LinkCodeGenerator;
import com.project.paypass_renewal.repository.UserRepository;
import com.project.paypass_renewal.support.UserDtoTestUtil;
import com.project.paypass_renewal.support.UserTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserAddressService userAddressService;

    @Mock
    UserCareGeofenceService userCareGeofenceService;

    @Mock
    WalletService walletService;

    @Mock
    LinkCodeGenerator linkCodeGenerator;

    @InjectMocks
    UserService userService;

    @Test
    @DisplayName("서비스_신규유저_저장_테스트")
    void saveNewUserTest() {
        // given
        UserRequestDto userRequestDto = UserDtoTestUtil.createDummyUserDto();
        String linkCode = "ABC123";
        // stub
        when(linkCodeGenerator.generate()).thenReturn(linkCode);
        when(userRepository.existsByLinkCode(linkCode)).thenReturn(false);

        // when
        User user = userService.saveNewUser(userRequestDto);

        // then
        assertThat(user.getNumber()).isNotNull();
        assertThat(user.getNumber()).isEqualTo(userRequestDto.getNumber());
        assertThat(user.getLinkCode()).isNotNull();
        assertThat(user.getLinkCode()).isEqualTo(linkCode);
    }

    @Test
    @DisplayName("서비스_신규유저_링크코드_중복_테스트")
    void linkCodeDuplicateTest(){
        // given
        UserRequestDto userRequestDto = UserDtoTestUtil.createDummyUserDto();
        String firstLinkCode = "123ABC";
        String secondLinkCode = "456DEF";
        // stub
        when(linkCodeGenerator.generate()).thenReturn(firstLinkCode,secondLinkCode);
        when(userRepository.existsByLinkCode(firstLinkCode)).thenReturn(true);
        when(userRepository.existsByLinkCode(secondLinkCode)).thenReturn(false);

        // when
        User user = userService.saveNewUser(userRequestDto);

        // then
        assertThat(user.getNumber()).isNotNull();
        assertThat(user.getNumber()).isEqualTo(userRequestDto.getNumber());
        assertThat(user.getLinkCode()).isNotNull();
        assertThat(user.getLinkCode()).isEqualTo(secondLinkCode);
    }

    @Test
    @DisplayName("유저_비밀번호_매치_불일치_테스트")
    void matchPasswordFalseTest() {
        // given
        LoginRequestDto loginRequestDto = new LoginRequestDto("01012345678", "abc111");
        User user = UserTestUtils.createDummyUser();

        // stub
        when(userRepository.findByNumber(any(String.class))).thenReturn(user);

        // when
        boolean result = userService.matchPassword(loginRequestDto);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("유저_비밀번호_매치_일치_테스트")
    void matchPasswordTrueTest() {
        // given
        LoginRequestDto loginRequestDto = new LoginRequestDto("01012345678", "abc123");
        User user = UserTestUtils.createDummyUser();

        // stub
        when(userRepository.findByNumber(any(String.class))).thenReturn(user);

        // when
        boolean result = userService.matchPassword(loginRequestDto);

        // then
        assertThat(result).isTrue();
    }


}















