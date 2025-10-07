package com.project.paypass_renewal.repository;

import com.project.paypass_renewal.domain.data.Station;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class StationRepositoryTest {

    @Autowired
    StationRepository stationRepository;

    @Test
    @DisplayName("stationNumber_busInfo_조회_테스트")
    void findBusInfoByStationNumberTest() {
        // given
        Station stationOne = new Station("테스트역1", 105000555L, new BigDecimal(127.03810910), new BigDecimal(37.57722935), "{100100017,46},{100100018,28}");
        Station stationTwo = new Station("테스트역2", 105000553L, new BigDecimal(127.03810123), new BigDecimal(37.57725431), "{100100017,47},{100100018,29}");

        stationRepository.save(stationOne);
        stationRepository.save(stationTwo);

        // when
        String busInfo = stationRepository.findBusInfoByStationNumber(105000555L);

        // then
        assertThat(busInfo).isEqualTo("{100100017,46},{100100018,28}");

    }



}
