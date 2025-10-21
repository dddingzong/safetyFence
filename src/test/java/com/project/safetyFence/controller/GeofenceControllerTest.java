package com.project.safetyFence.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.safetyFence.domain.Geofence;
import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.UserAddress;
import com.project.safetyFence.domain.dto.request.NumberRequestDto;
import com.project.safetyFence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GeofenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        testUser = new User("01012345678", "tester", "password", LocalDate.now(), "test-link");
        UserAddress userAddress = new UserAddress(testUser, "서울", "부산", "서울시 강남구", "상세주소1", "부산시 해운대구");

        // 지오펜스 생성 (startTime, endTime은 null로 설정)
        Geofence homeGeofence = new Geofence(testUser, "집", "서울시 노원구 노원로 564",
                new BigDecimal("37.123"), new BigDecimal("127.123"), 0, 100);
        Geofence centerGeofence = new Geofence(testUser, "센터", "서울시 노원구 노원로 18길 41",
                new BigDecimal("35.456"), new BigDecimal("129.456"), 0, 100);

        // 연관관계 설정
        testUser.addUserAddress(userAddress);
        testUser.addGeofence(homeGeofence);
        testUser.addGeofence(centerGeofence);

        // 저장
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("사용자 번호로 지오펜스 목록 조회 API 테스트")
    void findGeofenceList() throws Exception {
        // given
        NumberRequestDto requestDto = new NumberRequestDto(testUser.getNumber());
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/geofence/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].address").exists())
                .andExpect(jsonPath("$[0].latitude").exists())
                .andExpect(jsonPath("$[0].longitude").exists())
                .andExpect(jsonPath("$[1].name").exists())
                .andExpect(jsonPath("$[1].address").exists())
                .andExpect(jsonPath("$[1].latitude").exists())
                .andExpect(jsonPath("$[1].longitude").exists())
                .andDo(print());
    }
}