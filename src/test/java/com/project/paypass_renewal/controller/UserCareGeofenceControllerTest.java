package com.project.paypass_renewal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.paypass_renewal.domain.dto.request.NumberRequestDto;
import com.project.paypass_renewal.domain.dto.response.UserCareGeofenceResponseDto;
import com.project.paypass_renewal.service.UserCareGeofenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserCareGeofenceControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserCareGeofenceService userCareGeofenceService;

    @InjectMocks
    private UserCareGeofenceController userCareGeofenceController;

    @BeforeEach
    void init() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(userCareGeofenceController).build();
    }

    @Test
    @DisplayName("케어_지오펜스_조회_테스트")
    void getCareGeofenceTest() throws Exception {
        // given
        final String url = "/geofence/getCareGeofence";

        NumberRequestDto numberRequestDto = new NumberRequestDto("01012345678");
        String number = numberRequestDto.getNumber();

        UserCareGeofenceResponseDto userCareGeofenceResponseDto = new UserCareGeofenceResponseDto(number, new BigDecimal("37.66277080"), new BigDecimal("127.05514400"), new BigDecimal("37.63772280"), new BigDecimal("127.13790120"));

        // stub
        when(userCareGeofenceService.findUserCareGeofence(any(String.class))).thenReturn(userCareGeofenceResponseDto);

        String json = objectMapper.writeValueAsString(numberRequestDto);
        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(number))
                .andExpect(jsonPath("$.homeLatitude").value("37.6627708"))
                .andExpect(jsonPath("$.homeLongitude").value("127.055144"))
                .andExpect(jsonPath("$.centerLatitude").value("37.6377228"))
                .andExpect(jsonPath("$.centerLongitude").value("127.1379012"));
    }
}
