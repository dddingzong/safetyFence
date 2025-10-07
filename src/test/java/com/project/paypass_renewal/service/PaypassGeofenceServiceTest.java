package com.project.paypass_renewal.service;

import com.project.paypass_renewal.domain.PaypassGeofence;
import com.project.paypass_renewal.repository.PayPassGeofenceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class PaypassGeofenceServiceTest {

    @Mock
    PayPassGeofenceRepository payPassGeofenceRepository;

    @InjectMocks
    PaypassGeofenceService paypassGeofenceService;

    @Test
    @DisplayName("유저_진입시_지오펜스_저장_테스트")
    void createGeofenceLocationTest() {
        // given
        String number = "1234567890";
        Long stationNumber = 12345L;
        String busInfo = "testBusInfo";

        // when
        PaypassGeofence paypassGeofence = paypassGeofenceService.createGeofenceLocation(number, stationNumber, busInfo);

        //then
        assertThat(paypassGeofence.getNumber()).isEqualTo(number);
        assertThat(paypassGeofence.getStationNumber()).isEqualTo(stationNumber);
        assertThat(paypassGeofence.getBusInfo()).isEqualTo(busInfo);
    }


}
