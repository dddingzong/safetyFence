package com.project.paypass_renewal.service;

import com.project.paypass_renewal.domain.UserLocation;
import com.project.paypass_renewal.domain.dto.request.UserLocationRequestDto;
import com.project.paypass_renewal.repository.UserLocationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserLocationServiceTest {

    @Mock
    private UserLocationRepository userLocationRepository;

    @InjectMocks
    private UserLocationService userLocationService;

    @Test
    @DisplayName("서비스_유저위치_저장_테스트")
    void saveNewUserTest(){
        // given
        UserLocationRequestDto userLocationDto = new UserLocationRequestDto("01012345678", "37.6616521", "127.0561246");

        // when
        UserLocation userLocation = userLocationService.saveUserLocation(userLocationDto);

        // then
        assertThat(userLocation.getNumber()).isEqualTo(userLocationDto.getNumber());
        assertThat(userLocation.getLatitude()).isEqualTo(String.valueOf(userLocationDto.getLatitude()));
        assertThat(userLocation.getLongitude()).isEqualTo(String.valueOf(userLocationDto.getLongitude()));
    }

}










