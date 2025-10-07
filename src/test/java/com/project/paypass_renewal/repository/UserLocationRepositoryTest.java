package com.project.paypass_renewal.repository;

import com.project.paypass_renewal.domain.UserLocation;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UserLocationRepositoryTest {

    @Autowired
    UserLocationRepository userLocationRepository;

    @Test
    @DisplayName("실시간_위치_저장")
    void saveUserLocationTest() {
        // given
        UserLocation userLocation = new UserLocation("01012345678", BigDecimal.valueOf(37.6616521), BigDecimal.valueOf(127.0561246));

        // when
        UserLocation savedUserLocation = userLocationRepository.save(userLocation);

        // then
        Long id = savedUserLocation.getId();

        assertThat(id).isEqualTo(userLocation.getId());
    }

    @Test
    @DisplayName("유저_위치_리스트_조회")
    void findByNumberOrderBySavedTimeDescTest() throws InterruptedException {
        // given
        String number = "01012345678";

        UserLocation userLocationOne = new UserLocation(number, BigDecimal.valueOf(37.6616521), BigDecimal.valueOf(127.0561246));
        Thread.sleep(1000);
        UserLocation userLocationTwo = new UserLocation(number, BigDecimal.valueOf(37.6616523), BigDecimal.valueOf(127.0561213));
        Thread.sleep(1000);
        UserLocation userLocationThree = new UserLocation("01011112222", BigDecimal.valueOf(37.6616121), BigDecimal.valueOf(127.0561463));

        userLocationRepository.save(userLocationOne);
        userLocationRepository.save(userLocationTwo);
        userLocationRepository.save(userLocationThree);
        // when
        List<UserLocation> userLocations = userLocationRepository.findByNumberOrderBySavedTimeDesc(number);

        // then
        assertThat(userLocations).hasSize(2);
        assertThat(userLocations.get(0).getNumber()).isEqualTo(number);
        assertThat(userLocations.get(1).getNumber()).isEqualTo(number);
        assertThat(userLocations.get(0).getLatitude()).isEqualTo(userLocationTwo.getLatitude());
    }
}
