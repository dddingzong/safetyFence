package com.project.paypass_renewal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.paypass_renewal.domain.DetailLog;
import com.project.paypass_renewal.domain.data.Station;
import com.project.paypass_renewal.domain.dto.request.LogIdRequestDto;
import com.project.paypass_renewal.service.BusNumberService;
import com.project.paypass_renewal.service.DetailLogService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class DetailLogControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private DetailLogService detailLogService;

    @Mock
    private StationService stationService;

    @Mock
    private BusNumberService busNumberService;

    @InjectMocks
    private DetailLogController detailLogController;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(detailLogController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("상세 로그 조회 테스트")
    void getDetailLogListTest() throws Exception {
        // given
        final String url = "/detailLog/getDetailLogList";
        LogIdRequestDto logIdRequestDto = new LogIdRequestDto(1L);
        String json = objectMapper.writeValueAsString(logIdRequestDto);

        // stub
        when(detailLogService.findDetailLogsByLogId(any(LogIdRequestDto.class))).thenReturn(
                List.of(
                        new DetailLog(1L, 1L, "01012345678", LocalDateTime.now(), LocalDateTime.now().plusMinutes(2), 101123123L, "routeId1,routeId2"),
                        new DetailLog(2L,1L,"01012345678", LocalDateTime.now().plusMinutes(10), LocalDateTime.now().plusMinutes(12), 105653632L, "routeId3,routeId4")
                )
        );

        when(stationService.findByStationNumber(101123123L)).thenReturn(new Station("test_정류장A", 101123123L,new BigDecimal(128.123123), new BigDecimal(24.123213) , "test_정류장A_busInfo"));
        when(stationService.findByStationNumber(105653632L)).thenReturn(new Station("test_정류장B", 105653632L,new BigDecimal(127.123112), new BigDecimal(24.121231) , "test_정류장B_busInfo"));

        // busNumberService를 routeId 그대로 반환하도록 설정
        when(busNumberService.findBusNameByRouteId(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));


        // when
        ResultActions result = mockMvc.
                perform(MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                );

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].fenceInTime").exists())
                .andExpect(jsonPath("$[0].fenceOutTime").exists())
                .andExpect(jsonPath("$[0].stationNumber").value(101123123L))
                .andExpect(jsonPath("$[0].stationName").value("test_정류장A"))
                .andExpect(jsonPath("$[0].busNumberString").value("routeId1,routeId2"));
    }

}