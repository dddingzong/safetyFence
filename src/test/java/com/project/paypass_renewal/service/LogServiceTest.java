package com.project.paypass_renewal.service;

import com.project.paypass_renewal.domain.Log;
import com.project.paypass_renewal.domain.dto.request.NumberRequestDto;
import com.project.paypass_renewal.domain.dto.response.LogListResponseDto;
import com.project.paypass_renewal.repository.LogRepository;
import com.project.paypass_renewal.repository.UserRepository;
import com.project.paypass_renewal.support.UserTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Mock
    LogRepository logRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    LogService logService;

    @Test
    @DisplayName("전화번호로_로그_리스트_조회_테스트")
    void getLogsByNumberTest() {

        // given
        NumberRequestDto numberRequestDto = new NumberRequestDto("01012345678");

        List<Log> dummyLogs = new ArrayList<>(Arrays.asList(
                new Log("01012345678", LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(2).minusMinutes(50), "집", "센터"),
                new Log("01012345678", LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1).minusMinutes(40), "센터", "집")
                ));

        // stub
        when(logRepository.findByNumberOrderByDepartureTimeDesc(any(String.class))).thenReturn(dummyLogs);
        when(userRepository.findByNumber(any(String.class))).thenReturn(UserTestUtils.createDummyUser());

        // when
        List<LogListResponseDto> logListResponseDtoList = logService.getLogListByNumber(numberRequestDto);

        // then
        assertThat(logListResponseDtoList.get(0).getDepartureLocation()).isEqualTo("집");
        assertThat(logListResponseDtoList.get(1).getDepartureLocation()).isEqualTo("센터");
        assertThat(logListResponseDtoList.get(0).getArrivalLocation()).isEqualTo("센터");
        assertThat(logListResponseDtoList.get(1).getArrivalLocation()).isEqualTo("집");
        assertThat(logListResponseDtoList.get(0).getName()).isEqualTo("더미유저");
    }


}






