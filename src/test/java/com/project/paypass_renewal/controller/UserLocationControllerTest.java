package com.project.paypass_renewal.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.paypass_renewal.domain.dto.request.NumberRequestDto;
import com.project.paypass_renewal.domain.dto.request.UserLocationRequestDto;
import com.project.paypass_renewal.domain.dto.response.UserLocationResponseDto;
import com.project.paypass_renewal.service.UserLocationService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserLocationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserLocationService userLocationService;

    @InjectMocks
    private UserLocationController userLocationController;


    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(userLocationController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("유저위치_저장_테스트")
    void saveUserLocationTest() throws Exception{
        // given
        final String url = "/user/saveUserLocation";

        UserLocationRequestDto userLocationDto = new UserLocationRequestDto("01012345678", "37.6616521", "127.0561246");

        String json = objectMapper.writeValueAsString(userLocationDto);
        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );
        // then
        result.andExpect(status().isOk())
                .andExpect(content().string("saveSuccess"));
    }

    @Test
    @DisplayName("유저_최근_위치_조회_테스트")
    void getUserRecentLocationTest() throws Exception{
        // given
        final String url = "/user/getRecentUserLocation";

        NumberRequestDto numberRequestDto = new NumberRequestDto("01012345678");

        String json = objectMapper.writeValueAsString(numberRequestDto);
        // stub
        when(userLocationService.findRecentLocationByNumber(any(NumberRequestDto.class)))
                .thenReturn(new UserLocationResponseDto(new BigDecimal("37.6616521"), new BigDecimal("127.0561246")));

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );
        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").exists())
                .andExpect(jsonPath("$.longitude").exists());
    }
}
