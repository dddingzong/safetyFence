package com.project.paypass_renewal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.paypass_renewal.domain.dto.request.StationNumberRequestDto;
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

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class StationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    StationService stationService;

    @InjectMocks
    StationController stationController;

    @BeforeEach
    void init() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(stationController).build();
    }

    @Test
    @DisplayName("stationNumber로 위도 경도 조회 테스트")
    void getStationLatLngTest() throws Exception {
        // given
        String url = "/station/getStationLatLng";
        StationNumberRequestDto stationNumberRequestDto = new StationNumberRequestDto(1234512L);
        String json = objectMapper.writeValueAsString(stationNumberRequestDto);

        // stub
        when(stationService.getStationLatLng(any(StationNumberRequestDto.class))).thenReturn(Map.of("latitude", BigDecimal.valueOf(37.5661235), "longitude", BigDecimal.valueOf(126.9712380)));

        // when
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"latitude\":37.5661235,\"longitude\":126.9712380}"));

    }
}