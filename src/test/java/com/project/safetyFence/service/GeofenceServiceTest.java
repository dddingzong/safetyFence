package com.project.safetyFence.service;

import com.project.safetyFence.domain.Geofence;
import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.dto.request.FenceInRequestDto;
import com.project.safetyFence.domain.dto.request.GeofenceRequestDto;
import com.project.safetyFence.repository.GeofenceRepository;
import com.project.safetyFence.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class GeofenceServiceTest {

    @Autowired
    private GeofenceService geofenceService;

    @Autowired
    private GeofenceRepository geofenceRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Geofence permanentGeofence;
    private Geofence temporaryGeofence;

    private static final String TEST_NUMBER = "01012345678";

    @BeforeEach
    void setUp() {
        // Test user
        testUser = new User(TEST_NUMBER, "tester", "password", LocalDate.of(1990, 1, 1), "test-link");
        userRepository.save(testUser);

        // Permanent geofence (type = 0, maxSequence = 5)
        permanentGeofence = new Geofence(
                testUser,
                "Home",
                "Seoul Gangnam",
                new BigDecimal("37.123456"),
                new BigDecimal("127.123456"),
                0,
                5
        );
        geofenceRepository.save(permanentGeofence);

        // Temporary geofence (type = 1)
        temporaryGeofence = new Geofence(
                testUser,
                "Temporary Location",
                "Seoul Gangdong",
                new BigDecimal("37.654321"),
                new BigDecimal("127.654321"),
                1,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                0
        );
        geofenceRepository.save(temporaryGeofence);
    }

    @Test
    @DisplayName("userFenceIn - permanent geofence decreases maxSequence")
    void userFenceIn_PermanentGeofence_DecreasesMaxSequence() {
        // given
        int initialMaxSequence = permanentGeofence.getMaxSequence();
        FenceInRequestDto requestDto = new FenceInRequestDto(permanentGeofence.getId(), TEST_NUMBER);

        // when
        geofenceService.userFenceIn(requestDto);

        // then
        Geofence updated = geofenceRepository.findById(permanentGeofence.getId())
                .orElseThrow();
        assertThat(updated.getMaxSequence()).isEqualTo(initialMaxSequence - 1);
    }

    @Test
    @DisplayName("userFenceIn - permanent geofence with maxSequence 0 does not decrease")
    void userFenceIn_PermanentGeofence_MaxSequenceZero() {
        // given
        Geofence zeroSequenceGeofence = new Geofence(
                testUser,
                "Zero Sequence",
                "Seoul",
                new BigDecimal("37.111111"),
                new BigDecimal("127.111111"),
                0,
                0
        );
        geofenceRepository.save(zeroSequenceGeofence);
        FenceInRequestDto requestDto = new FenceInRequestDto(zeroSequenceGeofence.getId(), TEST_NUMBER);

        // when
        geofenceService.userFenceIn(requestDto);

        // then
        Geofence updated = geofenceRepository.findById(zeroSequenceGeofence.getId())
                .orElseThrow();
        assertThat(updated.getMaxSequence()).isEqualTo(0);
    }

    @Test
    @DisplayName("userFenceIn - temporary geofence is deleted")
    void userFenceIn_TemporaryGeofence_IsDeleted() {
        // given
        Long temporaryGeofenceId = temporaryGeofence.getId();
        FenceInRequestDto requestDto = new FenceInRequestDto(temporaryGeofenceId, TEST_NUMBER);

        // when
        geofenceService.userFenceIn(requestDto);

        // then
        assertThat(geofenceRepository.findById(temporaryGeofenceId)).isEmpty();
    }

    @Test
    @DisplayName("userFenceIn - geofence not found throws exception")
    void userFenceIn_GeofenceNotFound_ThrowsException() {
        // given
        Long nonExistentGeofenceId = 999999L;
        FenceInRequestDto requestDto = new FenceInRequestDto(nonExistentGeofenceId, TEST_NUMBER);

        // when & then
        assertThatThrownBy(() -> geofenceService.userFenceIn(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Geofence not found");
    }

    @Test
    @DisplayName("userFenceIn - multiple entries decrease maxSequence correctly")
    void userFenceIn_MultipleEntries_DecreasesCorrectly() {
        // given
        FenceInRequestDto requestDto = new FenceInRequestDto(permanentGeofence.getId(), TEST_NUMBER);
        int initialMaxSequence = permanentGeofence.getMaxSequence();

        // when - enter 3 times
        geofenceService.userFenceIn(requestDto);
        geofenceService.userFenceIn(requestDto);
        geofenceService.userFenceIn(requestDto);

        // then
        Geofence updated = geofenceRepository.findById(permanentGeofence.getId())
                .orElseThrow();
        assertThat(updated.getMaxSequence()).isEqualTo(initialMaxSequence - 3);
    }

    @Test
    @DisplayName("createNewFence - permanent geofence creation success")
    void createNewFence_PermanentGeofence_Success() {
        // given
        GeofenceRequestDto requestDto = new GeofenceRequestDto(
                TEST_NUMBER,
                "Test Location",
                "서울시 강남구 테헤란로 152",
                0,  // permanent type
                null,
                null
        );

        int initialCount = testUser.getGeofences().size();

        // when
        geofenceService.createNewFence(requestDto);

        // then
        User updatedUser = userRepository.findByNumber(TEST_NUMBER);
        assertThat(updatedUser.getGeofences()).hasSize(initialCount + 1);

        // Verify the new geofence
        Geofence newGeofence = updatedUser.getGeofences().stream()
                .filter(g -> "Test Location".equals(g.getName()))
                .findFirst()
                .orElseThrow();

        assertThat(newGeofence.getType()).isEqualTo(0);
        assertThat(newGeofence.getMaxSequence()).isEqualTo(999);
        assertThat(newGeofence.getUser()).isEqualTo(updatedUser);
    }

    @Test
    @DisplayName("createNewFence - temporary geofence creation success")
    void createNewFence_TemporaryGeofence_Success() {
        // given
        String startTime = "2025-10-21T10:00:00";
        String endTime = "2025-10-21T18:00:00";

        GeofenceRequestDto requestDto = new GeofenceRequestDto(
                TEST_NUMBER,
                "Temporary Event",
                "서울시 강남구 역삼동",
                1,  // temporary type
                startTime,
                endTime
        );

        int initialCount = testUser.getGeofences().size();

        // when
        geofenceService.createNewFence(requestDto);

        // then
        User updatedUser = userRepository.findByNumber(TEST_NUMBER);
        assertThat(updatedUser.getGeofences()).hasSize(initialCount + 1);

        // Verify the new temporary geofence
        Geofence newGeofence = updatedUser.getGeofences().stream()
                .filter(g -> "Temporary Event".equals(g.getName()))
                .findFirst()
                .orElseThrow();

        assertThat(newGeofence.getType()).isEqualTo(1);
        assertThat(newGeofence.getMaxSequence()).isEqualTo(100);
        assertThat(newGeofence.getStartTime()).isNotNull();
        assertThat(newGeofence.getEndTime()).isNotNull();
    }
}
