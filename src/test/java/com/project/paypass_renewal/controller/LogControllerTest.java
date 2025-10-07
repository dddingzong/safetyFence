package com.project.paypass_renewal.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.paypass_renewal.domain.dto.request.NumberRequestDto;
import com.project.paypass_renewal.domain.dto.response.LogListResponseDto;
import com.project.paypass_renewal.service.LogService;
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
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LogControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    LogService logService;

    @InjectMocks
    LogController logController;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(logController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("컨트롤러_유저_로그_리스트_전송_테스트")
    void getLogListTest() throws Exception{
        // given
        final String url = "/log/getLogList";
        NumberRequestDto numberRequestDto = new NumberRequestDto("01089099721");
        String json = objectMapper.writeValueAsString(numberRequestDto);

        LogListResponseDto logListResponseDto = new LogListResponseDto(1L,"01089099721", "정종인", LocalDateTime.of(2025, 6, 20, 9, 0), LocalDateTime.of(2025, 6, 20, 9, 40), "집", "센터");
        List<LogListResponseDto> logList = new ArrayList<>();
        logList.add(logListResponseDto);

        // stub
        when(logService.getLogListByNumber(any(NumberRequestDto.class))).thenReturn(logList);

        // when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].number").value("01089099721"))
                .andExpect(jsonPath("$[0].name").value("정종인"))
                .andExpect(jsonPath("$[0].departureLocation").value("집"))
                .andExpect(jsonPath("$[0].arrivalLocation").value("센터"));




    }

}