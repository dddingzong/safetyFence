package com.project.paypass_renewal.util.algorithm;


import com.project.paypass_renewal.domain.UserCareGeofence;
import com.project.paypass_renewal.domain.dto.request.CareGeofenceMoveDto;
import com.project.paypass_renewal.domain.dto.request.HistoryEntryDto;
import com.project.paypass_renewal.repository.UserCareGeofenceRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class CareGeofenceTimeAlgorithmTest {

    @Autowired
    CareGeofenceTimeAlgorithm  careGeofenceTimeAlgorithm;

    @Autowired
    UserCareGeofenceRepository userCareGeofenceRepository;

    @Test
    @DisplayName("케어서비스_알고리즘_참_테스트")
    void careGeofenceTimeAlgorithmTrueTest() {
        // given
        String number = "01012345678";

        UserCareGeofence userCareGeofence = new UserCareGeofence(number,
                new BigDecimal("37.66277080"),
                new BigDecimal("127.05514400"),
                new BigDecimal("37.63772280"),
                new BigDecimal("127.13790120"));

        userCareGeofenceRepository.save(userCareGeofence);

        CareGeofenceMoveDto careGeofenceMoveDto = new CareGeofenceMoveDto(number, List.of(
                new HistoryEntryDto("집", LocalDateTime.of(2023, 10, 1, 8, 0)),
                new HistoryEntryDto("센터", LocalDateTime.of(2023, 10, 1, 8, 30))
        ));
        // when
        boolean result = careGeofenceTimeAlgorithm.careTimeAlgorithm(careGeofenceMoveDto);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("케어서비스_알고리즘_거짓_테스트")
    void careGeofenceTimeAlgorithmFalseTest() {
        // given
        String number = "01012345678";

        UserCareGeofence userCareGeofence = new UserCareGeofence(number,
                new BigDecimal("37.66277080"),
                new BigDecimal("127.05514400"),
                new BigDecimal("37.63772280"),
                new BigDecimal("127.13790120"));

        userCareGeofenceRepository.save(userCareGeofence);

        CareGeofenceMoveDto careGeofenceMoveDto = new CareGeofenceMoveDto(number, List.of(
                new HistoryEntryDto("집", LocalDateTime.of(2023, 10, 1, 8, 0)),
                new HistoryEntryDto("센터", LocalDateTime.of(2023, 10, 1, 8, 50))
        ));
        // when
        boolean result = careGeofenceTimeAlgorithm.careTimeAlgorithm(careGeofenceMoveDto);

        // then
        assertThat(result).isFalse();
    }



}