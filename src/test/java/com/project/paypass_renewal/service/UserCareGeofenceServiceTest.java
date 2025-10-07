package com.project.paypass_renewal.service;

import com.project.paypass_renewal.domain.ServiceCode;
import com.project.paypass_renewal.domain.UserCareGeofence;
import com.project.paypass_renewal.domain.dto.request.CareGeofenceMoveDto;
import com.project.paypass_renewal.domain.dto.request.HistoryEntryDto;
import com.project.paypass_renewal.domain.dto.request.NumberRequestDto;
import com.project.paypass_renewal.domain.dto.request.UserRequestDto;
import com.project.paypass_renewal.domain.dto.response.UserCareGeofenceResponseDto;
import com.project.paypass_renewal.repository.UserCareGeofenceRepository;
import com.project.paypass_renewal.util.algorithm.CareGeofenceTimeAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCareGeofenceServiceTest {

    @Mock
    private UserCareGeofenceRepository userCareGeofenceRepository;

    @Mock
    private LogService logService;

    @Mock
    private CareGeofenceTimeAlgorithm careGeofenceTimeAlgorithm;

    @InjectMocks
    private UserCareGeofenceService userCareGeofenceService;

    @Test
    @DisplayName("유저_지오펜스_저장_테스트")
    void saveUserGeofenceTest() {
        // given
        UserRequestDto userRequestDto = new UserRequestDto("정종인",
                "abc123",
                LocalDate.of(2023, 10, 1),
                "01012345678",
                "01675",
                "01747",
                ServiceCode.CARE_SERVICE,
                "서울시 노원구 노원로 564",
                "102-100",
                "서울 노원구 노원로18길 41");

        // when
        UserCareGeofence userCareGeofence = userCareGeofenceService.saveUserGeofence(userRequestDto);

        // then
        assertThat(userCareGeofence).isNotNull();
        assertThat(userCareGeofence.getNumber()).isEqualTo("01012345678");
        assertThat(userCareGeofence.getHomeLatitude()).isNotNull();
        assertThat(userCareGeofence.getHomeLongitude()).isNotNull();
        assertThat(userCareGeofence.getCenterLatitude()).isNotNull();
        assertThat(userCareGeofence.getCenterLongitude()).isNotNull();
    }

    @Test
    @DisplayName("유저_지오펜스_조회_테스트")
    void findUserGeofenceTest() {
        // given
        NumberRequestDto numberRequestDto = new NumberRequestDto("01012345678");
        String number = numberRequestDto.getNumber();
        UserCareGeofence userCareGeofence = new UserCareGeofence(number, new BigDecimal("37.66277080"), new BigDecimal("127.05514400"), new BigDecimal("37.63772280"), new BigDecimal("127.13790120"));

        // when
        when(userCareGeofenceRepository.findByNumber(any(String.class))).thenReturn(userCareGeofence);
        UserCareGeofenceResponseDto userCareGeofenceResponseDto = userCareGeofenceService.findUserCareGeofence(number);

        // then
        assertThat(userCareGeofenceResponseDto).isNotNull();
        assertThat(userCareGeofenceResponseDto.getNumber()).isEqualTo(number);
    }

    @Test
    @DisplayName("유저_지오펜스_조회_null_테스트")
    void findUserGeofenceCenterAddressNullTest() {
        // given
        String number = "01012345678";
        UserCareGeofence userCareGeofence = new UserCareGeofence(number, new BigDecimal("37.66277080"), new BigDecimal("127.05514400"), null, null);

        // when
        when(userCareGeofenceRepository.findByNumber(any(String.class))).thenReturn(userCareGeofence);
        UserCareGeofenceResponseDto userCareGeofenceResponseDto = userCareGeofenceService.findUserCareGeofence(number);

        // then
        assertThat(userCareGeofenceResponseDto).isNull();
    }

    @Test
    @DisplayName("유저_지오펜스_저장_후보_서비스_테스트")
    void checkCareGeofenceAlgorithmTest() {
        // given
        CareGeofenceMoveDto careGeofenceMoveDto =
                new CareGeofenceMoveDto("01012345678",
                        List.of(new HistoryEntryDto("집", LocalDate.now().atStartOfDay().plusMinutes(30)),
                                new HistoryEntryDto("센터", LocalDate.now().atStartOfDay().plusMinutes(46)))
                );

        // stub
        when(careGeofenceTimeAlgorithm.careTimeAlgorithm(any(CareGeofenceMoveDto.class))).thenReturn(true);

        // when
        String result = userCareGeofenceService.checkCareGeofenceAlgorithm(careGeofenceMoveDto);

        // then
        assertThat(result).isEqualTo("successSaveLog");
    }

}
