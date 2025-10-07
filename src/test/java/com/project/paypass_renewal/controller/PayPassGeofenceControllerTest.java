package com.project.paypass_renewal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.paypass_renewal.domain.PaypassGeofence;
import com.project.paypass_renewal.domain.dto.request.UserPaypassGeofenceRequestDto;
import com.project.paypass_renewal.service.PaypassGeofenceService;
import com.project.paypass_renewal.service.StationService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PayPassGeofenceControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    PaypassGeofenceService paypassGeofenceService;

    @Mock
    StationService stationService;

    @InjectMocks
    PayPassGeofenceController payPassGeofenceController;

    @BeforeEach
    void init() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(payPassGeofenceController).build();
    }

    @Test
    @DisplayName("사용자가 geofence에 진입했을 때, paypassGeofence 생성 테스트")
    void userFenceInTest() throws Exception {
        // given
        final String url = "/geofence/userFenceIn";

        UserPaypassGeofenceRequestDto requestDto = new UserPaypassGeofenceRequestDto("01012345678", 12345L, "testName");
        String json = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // then
        result.andExpect(status().isOk())
              .andExpect(content().string("success save geofence data"));
    }

    @Test
    @DisplayName("정류장_이탈_entity_미존재_테스트")
    void userFenceOutNoEntityTest() throws Exception {
        // given
        final String url = "/geofence/userFenceOut";

        UserPaypassGeofenceRequestDto requestDto = new UserPaypassGeofenceRequestDto("01012345678", 12345L, "testName");
        PaypassGeofence paypassGeofence = new PaypassGeofence("01012345678", 12345L, "savedTestBusInfo");
        String json = objectMapper.writeValueAsString(requestDto);

        // stub
        when(paypassGeofenceService.findByNumberAndStationNumber(any(String.class), any(Long.class))).thenReturn(List.of());
        when(stationService.findBusInfoByStationNumber(any(Long.class))).thenReturn("savedTestBusInfo");
        when(paypassGeofenceService.userFenceOutWithoutEntity(any(String.class), any(Long.class), any(String.class), any())).thenReturn(paypassGeofence);

        // when
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // then
        result.andExpect(status().isOk())
              .andExpect(content().string("success update fenceOutTime"));

    }

    @Test
    @DisplayName("정류장_이탈_entity_단일_존재_테스트")
    void userFenceOutSingleEntityTest() throws Exception {
        // given
        final String url = "/geofence/userFenceOut";

        UserPaypassGeofenceRequestDto requestDto = new UserPaypassGeofenceRequestDto("01012345678", 12345L, "testName");
        List<PaypassGeofence> paypassGeofences = List.of(
                new PaypassGeofence("01012345678", 12345L, "testBusInfo")
        );

        String json = objectMapper.writeValueAsString(requestDto);

        // stub
        when(paypassGeofenceService.findByNumberAndStationNumber(any(String.class), any(Long.class))).thenReturn(paypassGeofences);

        // when
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // then
        result.andExpect(status().isOk())
              .andExpect(content().string("success update fenceOutTime"));
    }

    @Test
    @DisplayName("정류장_이탈_entity_다중_존재_테스트")
    void userFenceOutMultipleEntitiesTest() throws Exception {
        // given
        final String url = "/geofence/userFenceOut";

        UserPaypassGeofenceRequestDto requestDto = new UserPaypassGeofenceRequestDto("01012345678", 12345L, "testName");
        PaypassGeofence paypassGeofenceFirst = new PaypassGeofence("01012345678", 12346L, "testBusInfoTwo");
        PaypassGeofence paypassGeofenceSecond = new PaypassGeofence("01012345678", 12345L, "testBusInfoOne", LocalDateTime.now().minusMinutes(1));

        List<PaypassGeofence> paypassGeofences = List.of(paypassGeofenceFirst, paypassGeofenceSecond);

        String json = objectMapper.writeValueAsString(requestDto);

        // stub
        when(paypassGeofenceService.findByNumberAndStationNumber(any(String.class), any(Long.class))).thenReturn(paypassGeofences);
        when(paypassGeofenceService.fenceOutTimeIsNull(paypassGeofenceFirst)).thenReturn(true);

        // when
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // then
        result.andExpect(status().isOk())
              .andExpect(content().string("success update fenceOutTime"));

        // verify
        verify(paypassGeofenceService).userFenceOut(paypassGeofenceFirst);
    }
}
