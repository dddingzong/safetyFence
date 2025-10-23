package com.project.safetyFence.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.safetyFence.domain.Geofence;
import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.UserAddress;
import com.project.safetyFence.domain.dto.request.FenceInRequestDto;
import com.project.safetyFence.repository.GeofenceRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
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

    @Autowired
    private GeofenceRepository geofenceRepository;

    private User testUser;
    private Geofence permanentGeofence;
    private Geofence temporaryGeofence;
    private String testApiKey;

    @BeforeEach
    void setUp() {
        // Test user with API Key
        testUser = new User("01012345678", "tester", "password", LocalDate.now(), "test-link");
        testApiKey = "test-api-key-12345678901234567890123456789012";
        testUser.updateApiKey(testApiKey);

        UserAddress userAddress = new UserAddress(testUser, "서울", "부산", "서울시 강남구", "상세주소1", "부산시 해운대구");
        testUser.addUserAddress(userAddress);

        // Permanent geofences
        Geofence homeGeofence = new Geofence(testUser, "집", "서울시 노원구 노원로 564",
                new BigDecimal("37.123"), new BigDecimal("127.123"), 0, 100);
        Geofence centerGeofence = new Geofence(testUser, "센터", "서울시 노원구 노원로 18길 41",
                new BigDecimal("35.456"), new BigDecimal("129.456"), 0, 100);

        // Permanent geofence for userFenceIn test
        permanentGeofence = new Geofence(testUser, "Test Permanent", "Seoul Test Address",
                new BigDecimal("37.555"), new BigDecimal("127.555"), 0, 5);

        // Temporary geofence for userFenceIn test
        temporaryGeofence = new Geofence(testUser, "Test Temporary", "Seoul Temp Address",
                new BigDecimal("37.666"), new BigDecimal("127.666"), 1,
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), 0);

        // Save user first
        userRepository.save(testUser);

        // Setup bidirectional relationships
        testUser.addGeofence(homeGeofence);
        testUser.addGeofence(centerGeofence);
        testUser.addGeofence(permanentGeofence);
        testUser.addGeofence(temporaryGeofence);

        // In tests, save individually to get IDs immediately
        // (In production, cascade would handle this, but tests need IDs right away)
        geofenceRepository.save(homeGeofence);
        geofenceRepository.save(centerGeofence);
        geofenceRepository.save(permanentGeofence);
        geofenceRepository.save(temporaryGeofence);
    }

    @Test
    @DisplayName("사용자 번호로 지오펜스 목록 조회 API 테스트")
    void findGeofenceList() throws Exception {
        // when & then
        mockMvc.perform(post("/geofence/list")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].address").exists())
                .andExpect(jsonPath("$[0].latitude").exists())
                .andExpect(jsonPath("$[0].longitude").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("userFenceIn - permanent geofence success")
    void userFenceIn_PermanentGeofence_Success() throws Exception {
        // given
        int initialMaxSequence = permanentGeofence.getMaxSequence();
        FenceInRequestDto requestDto = new FenceInRequestDto(permanentGeofence.getId());
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/geofence/userFenceIn")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("사용자의 진입이 성공적으로 감지되었습니다."))
                .andDo(print());

        // Verify maxSequence decreased
        Geofence updated = geofenceRepository.findById(permanentGeofence.getId())
                .orElseThrow();
        assertThat(updated.getMaxSequence()).isEqualTo(initialMaxSequence - 1);
    }

    @Test
    @DisplayName("userFenceIn - temporary geofence deleted")
    void userFenceIn_TemporaryGeofence_Deleted() throws Exception {
        // given
        Long temporaryGeofenceId = temporaryGeofence.getId();
        FenceInRequestDto requestDto = new FenceInRequestDto(temporaryGeofenceId);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/geofence/userFenceIn")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("사용자의 진입이 성공적으로 감지되었습니다."))
                .andDo(print());

        // Verify temporary geofence was deleted
        assertThat(geofenceRepository.findById(temporaryGeofenceId)).isEmpty();
    }

    @Test
    @DisplayName("userFenceIn - multiple entries decrease sequence")
    void userFenceIn_MultipleEntries_DecreaseSequence() throws Exception {
        // given
        int initialMaxSequence = permanentGeofence.getMaxSequence();
        FenceInRequestDto requestDto = new FenceInRequestDto(permanentGeofence.getId());
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when - enter 3 times
        mockMvc.perform(post("/geofence/userFenceIn")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        mockMvc.perform(post("/geofence/userFenceIn")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        mockMvc.perform(post("/geofence/userFenceIn")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        // then
        Geofence updated = geofenceRepository.findById(permanentGeofence.getId())
                .orElseThrow();
        assertThat(updated.getMaxSequence()).isEqualTo(initialMaxSequence - 3);
    }
}