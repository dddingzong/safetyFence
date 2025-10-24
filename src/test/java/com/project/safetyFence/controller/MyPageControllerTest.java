package com.project.safetyFence.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.safetyFence.domain.Geofence;
import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.UserAddress;
import com.project.safetyFence.domain.dto.request.CenterAddressUpdateRequestDto;
import com.project.safetyFence.domain.dto.request.HomeAddressUpdateRequestDto;
import com.project.safetyFence.domain.dto.request.PasswordUpdateRequestDto;
import com.project.safetyFence.repository.UserRepository;
import jakarta.persistence.EntityManager;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

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

    // ========== Password Update Tests ==========

    @Test
    @DisplayName("updatePassword - 비밀번호 변경 성공")
    void updatePassword_Success() throws Exception {
        // given
        PasswordUpdateRequestDto requestDto = new PasswordUpdateRequestDto(
                "password123",
                "newPassword456"
        );

        // when
        mockMvc.perform(patch("/mypage/password")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("비밀번호가 성공적으로 변경되었습니다."))
                .andDo(print());

        // then
        entityManager.flush();
        entityManager.clear();

        User updatedUser = userRepository.findByNumber("01012345678");
        assertThat(updatedUser.getPassword()).isEqualTo("newPassword456");
    }

    @Test
    @DisplayName("updatePassword - 현재 비밀번호 불일치 시 실패")
    void updatePassword_WrongCurrentPassword_Fail() throws Exception {
        // given
        PasswordUpdateRequestDto requestDto = new PasswordUpdateRequestDto(
                "wrongPassword",
                "newPassword456"
        );

        // when & then
        mockMvc.perform(patch("/mypage/password")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("updatePassword - API Key 없이 요청 시 401 에러")
    void updatePassword_NoApiKey_Unauthorized() throws Exception {
        // given
        PasswordUpdateRequestDto requestDto = new PasswordUpdateRequestDto(
                "password123",
                "newPassword456"
        );

        // when & then
        mockMvc.perform(patch("/mypage/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("updatePassword - 잘못된 API Key로 요청 시 401 에러")
    void updatePassword_InvalidApiKey_Unauthorized() throws Exception {
        // given
        PasswordUpdateRequestDto requestDto = new PasswordUpdateRequestDto(
                "password123",
                "newPassword456"
        );

        // when & then
        mockMvc.perform(patch("/mypage/password")
                        .header("X-API-Key", "invalid-api-key-12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    // ========== Home Address Update Tests ==========

    @Test
    @DisplayName("updateHomeAddress - 집주소 변경 성공")
    void updateHomeAddress_Success() throws Exception {
        // given
        HomeAddressUpdateRequestDto requestDto = new HomeAddressUpdateRequestDto(
                "06234",
                "서울시 강남구 역삼로 789",
                "202호"
        );

        // when
        mockMvc.perform(patch("/mypage/homeAddress")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("집주소가 성공적으로 변경되었습니다."))
                .andDo(print());

        // then
        entityManager.flush();
        entityManager.clear();

        User updatedUser = userRepository.findByNumber("01012345678");
        UserAddress updatedAddress = updatedUser.getUserAddress();
        assertThat(updatedAddress.getHomeAddress()).isEqualTo("06234");
        assertThat(updatedAddress.getHomeStreetAddress()).isEqualTo("서울시 강남구 역삼로 789");
        assertThat(updatedAddress.getHomeStreetAddressDetail()).isEqualTo("202호");
    }

    @Test
    @DisplayName("updateHomeAddress - 우편번호만 변경")
    void updateHomeAddress_OnlyPostalCode_Success() throws Exception {
        // given
        HomeAddressUpdateRequestDto requestDto = new HomeAddressUpdateRequestDto(
                "12345",
                "서울시 강남구 테헤란로 123",
                "101호"
        );

        // when
        mockMvc.perform(patch("/mypage/homeAddress")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("집주소가 성공적으로 변경되었습니다."))
                .andDo(print());

        // then
        entityManager.flush();
        entityManager.clear();

        User updatedUser = userRepository.findByNumber("01012345678");
        UserAddress updatedAddress = updatedUser.getUserAddress();
        assertThat(updatedAddress.getHomeAddress()).isEqualTo("12345");
    }

    @Test
    @DisplayName("updateHomeAddress - API Key 없이 요청 시 401 에러")
    void updateHomeAddress_NoApiKey_Unauthorized() throws Exception {
        // given
        HomeAddressUpdateRequestDto requestDto = new HomeAddressUpdateRequestDto(
                "06234",
                "서울시 강남구 역삼로 789",
                "202호"
        );

        // when & then
        mockMvc.perform(patch("/mypage/homeAddress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    // ========== Center Address Update Tests ==========

    @Test
    @DisplayName("updateCenterAddress - 센터주소 변경 성공")
    void updateCenterAddress_Success() throws Exception {
        // given
        CenterAddressUpdateRequestDto requestDto = new CenterAddressUpdateRequestDto(
                "48058",
                "부산시 해운대구 해운대로 999"
        );

        // when
        mockMvc.perform(patch("/mypage/centerAddress")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("센터주소가 성공적으로 변경되었습니다."))
                .andDo(print());

        // then
        entityManager.flush();
        entityManager.clear();

        User updatedUser = userRepository.findByNumber("01012345678");
        UserAddress updatedAddress = updatedUser.getUserAddress();
        assertThat(updatedAddress.getCenterAddress()).isEqualTo("48058");
        assertThat(updatedAddress.getCenterStreetAddress()).isEqualTo("부산시 해운대구 해운대로 999");
    }

    @Test
    @DisplayName("updateCenterAddress - 우편번호만 변경")
    void updateCenterAddress_OnlyPostalCode_Success() throws Exception {
        // given
        CenterAddressUpdateRequestDto requestDto = new CenterAddressUpdateRequestDto(
                "67890",
                "부산시 해운대구 센텀로 456"
        );

        // when
        mockMvc.perform(patch("/mypage/centerAddress")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("센터주소가 성공적으로 변경되었습니다."))
                .andDo(print());

        // then
        entityManager.flush();
        entityManager.clear();

        User updatedUser = userRepository.findByNumber("01012345678");
        UserAddress updatedAddress = updatedUser.getUserAddress();
        assertThat(updatedAddress.getCenterAddress()).isEqualTo("67890");
    }

    @Test
    @DisplayName("updateCenterAddress - API Key 없이 요청 시 401 에러")
    void updateCenterAddress_NoApiKey_Unauthorized() throws Exception {
        // given
        CenterAddressUpdateRequestDto requestDto = new CenterAddressUpdateRequestDto(
                "48058",
                "부산시 해운대구 해운대로 999"
        );

        // when & then
        mockMvc.perform(patch("/mypage/centerAddress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("updateCenterAddress - 잘못된 API Key로 요청 시 401 에러")
    void updateCenterAddress_InvalidApiKey_Unauthorized() throws Exception {
        // given
        CenterAddressUpdateRequestDto requestDto = new CenterAddressUpdateRequestDto(
                "48058",
                "부산시 해운대구 해운대로 999"
        );

        // when & then
        mockMvc.perform(patch("/mypage/centerAddress")
                        .header("X-API-Key", "invalid-api-key-12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}
