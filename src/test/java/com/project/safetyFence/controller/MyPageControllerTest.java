package com.project.safetyFence.controller;

import com.project.safetyFence.domain.Geofence;
import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.UserAddress;
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
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MyPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private String testApiKey;

    @BeforeEach
    void setUp() {
        // Test user with API Key
        testUser = new User("01012345678", "홍길동", "password123", LocalDate.of(1990, 5, 15), "test-link-code");
        testApiKey = "test-api-key-12345678901234567890123456789012";
        testUser.updateApiKey(testApiKey);

        // User address
        UserAddress userAddress = new UserAddress(
                testUser,
                "서울시",
                "부산시",
                "서울시 강남구 테헤란로 123",
                "101호",
                "부산시 해운대구 센텀로 456"
        );
        testUser.addUserAddress(userAddress);

        // Geofences
        Geofence homeGeofence = new Geofence(
                testUser,
                "집",
                "서울시 강남구 테헤란로 123",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                0,
                100
        );
        testUser.addGeofence(homeGeofence);

        Geofence centerGeofence = new Geofence(
                testUser,
                "센터",
                "부산시 해운대구 센텀로 456",
                new BigDecimal("35.1796"),
                new BigDecimal("129.0756"),
                0,
                100
        );
        testUser.addGeofence(centerGeofence);

        Geofence temporaryGeofence = new Geofence(
                testUser,
                "임시 장소",
                "서울시 종로구 청계천로 100",
                new BigDecimal("37.5695"),
                new BigDecimal("126.9769"),
                1,
                LocalDateTime.of(2024, 10, 25, 9, 0),
                LocalDateTime.of(2024, 10, 25, 18, 0),
                0
        );
        testUser.addGeofence(temporaryGeofence);

        // Save user (cascade will save address and geofences)
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("getMyPageData - 마이페이지 데이터 조회 성공")
    void getMyPageData_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/get/myPageData")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.birth").value("1990-05-15"))
                .andExpect(jsonPath("$.homeAddress").value("서울시 강남구 테헤란로 123 101호"))
                .andExpect(jsonPath("$.centerAddress").value("부산시 해운대구 센텀로 456"))
                .andExpect(jsonPath("$.linkCode").value("test-link-code"))
                .andExpect(jsonPath("$.geofences").isArray())
                .andExpect(jsonPath("$.geofences.length()").value(3))
                .andExpect(jsonPath("$.geofences[0].name").exists())
                .andExpect(jsonPath("$.geofences[0].address").exists())
                .andExpect(jsonPath("$.geofences[0].type").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("getMyPageData - API Key 없이 요청 시 401 에러")
    void getMyPageData_NoApiKey_Unauthorized() throws Exception {
        // when & then
        mockMvc.perform(get("/get/myPageData")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("getMyPageData - 잘못된 API Key로 요청 시 401 에러")
    void getMyPageData_InvalidApiKey_Unauthorized() throws Exception {
        // when & then
        mockMvc.perform(get("/get/myPageData")
                        .header("X-API-Key", "invalid-api-key-12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("getMyPageData - geofence 정보가 올바르게 반환되는지 확인")
    void getMyPageData_VerifyGeofenceDetails() throws Exception {
        // when & then
        mockMvc.perform(get("/get/myPageData")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.geofences[?(@.name == '집')]").exists())
                .andExpect(jsonPath("$.geofences[?(@.name == '센터')]").exists())
                .andExpect(jsonPath("$.geofences[?(@.name == '임시 장소')]").exists())
                .andExpect(jsonPath("$.geofences[?(@.type == 0)]").exists())
                .andExpect(jsonPath("$.geofences[?(@.type == 1)]").exists())
                .andDo(print());
    }
}
