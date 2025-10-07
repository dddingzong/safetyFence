package com.project.paypass_renewal.repository;


import com.project.paypass_renewal.domain.PaypassGeofence;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class PayPassGeofenceRepositoryTest {

    @Autowired
    PayPassGeofenceRepository payPassGeofenceRepository;

    @Test
    @DisplayName("stationNumber와 number로 paypassGeofence 조회")
    void findByNumberAndStationNumberTest() {
        // given
        String number = "1234567890";
        Long stationNumber = 12345L;
        String busInfo = "testBusInfo";

        PaypassGeofence paypassGeofence = new PaypassGeofence(number, stationNumber, busInfo);
        payPassGeofenceRepository.save(paypassGeofence);

        // when
        List<PaypassGeofence> result = payPassGeofenceRepository.findByNumberAndStationNumber(number, stationNumber);

        // then
        Assertions.assertThat(result.get(0)).isNotNull();
        Assertions.assertThat(result.get(0).getNumber()).isEqualTo(number);
        Assertions.assertThat(result.get(0).getStationNumber()).isEqualTo(stationNumber);
        Assertions.assertThat(result.get(0).getBusInfo()).isEqualTo(busInfo);
    }


}