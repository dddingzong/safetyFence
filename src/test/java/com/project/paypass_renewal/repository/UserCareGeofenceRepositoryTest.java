package com.project.paypass_renewal.repository;

import com.project.paypass_renewal.domain.UserCareGeofence;
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
class UserCareGeofenceRepositoryTest {

    @Autowired
    UserCareGeofenceRepository userCareGeofenceRepository;

    @Test
    @DisplayName("유저_지오펜스_저장_테스트")
    void saveUserCareGeofenceTest() {
        // given
        String number = "01012345678";
        BigDecimal homeLatitude = new BigDecimal("37.66277080");
        BigDecimal homeLongitude = new BigDecimal("127.05514400");
        BigDecimal centerLatitude = new BigDecimal("37.63772280");
        BigDecimal centerLongitude = new BigDecimal("127.13790120");

        UserCareGeofence userCareGeofence = new UserCareGeofence(number, homeLatitude, homeLongitude, centerLatitude, centerLongitude);

        // when
        UserCareGeofence savedGeofence = userCareGeofenceRepository.save(userCareGeofence);

        // then
        assertThat(savedGeofence.getNumber()).isEqualTo(number);
        assertThat(savedGeofence.getHomeLatitude()).isEqualTo(homeLatitude);
        assertThat(savedGeofence.getHomeLongitude()).isEqualTo(homeLongitude);
        assertThat(savedGeofence.getCenterLatitude()).isEqualTo(centerLatitude);
        assertThat(savedGeofence.getCenterLongitude()).isEqualTo(centerLongitude);
    }

    @Test
    @DisplayName("유저_지오펜스_조회_테스트")
    void findUserCareGeofenceTest() {
        // given
        String number = "01012345678";
        BigDecimal homeLatitude = new BigDecimal("37.66277080");
        BigDecimal homeLongitude = new BigDecimal("127.05514400");
        BigDecimal centerLatitude = new BigDecimal("37.63772280");
        BigDecimal centerLongitude = new BigDecimal("127.13790120");

        UserCareGeofence userCareGeofence = new UserCareGeofence(number, homeLatitude, homeLongitude, centerLatitude, centerLongitude);
        userCareGeofenceRepository.save(userCareGeofence);

        // when
        UserCareGeofence foundGeofence = userCareGeofenceRepository.findByNumber(number);

        // then
        assertThat(foundGeofence).isNotNull();
        assertThat(foundGeofence.getNumber()).isEqualTo(number);
        assertThat(foundGeofence.getHomeLatitude()).isEqualTo(homeLatitude);
        assertThat(foundGeofence.getHomeLongitude()).isEqualTo(homeLongitude);
        assertThat(foundGeofence.getCenterLatitude()).isEqualTo(centerLatitude);
        assertThat(foundGeofence.getCenterLongitude()).isEqualTo(centerLongitude);
    }

}
