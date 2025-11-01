package com.project.safetyFence.service;

import com.project.safetyFence.geofence.domain.Geofence;
import com.project.safetyFence.user.domain.User;
import com.project.safetyFence.geofence.dto.GeofenceRequestDto;
import com.project.safetyFence.geofence.GeofenceRepository;
import com.project.safetyFence.user.UserRepository;
import com.project.safetyFence.geofence.GeofenceService;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Geofence permanentGeofence;
    private Geofence temporaryGeofence;

    private static final String TEST_NUMBER = "01012345678";

    @BeforeEach
    void setUp() {
        // Test user
        testUser = new User(TEST_NUMBER, "tester", "password", LocalDate.of(1990, 1, 1), "test-link");

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

        // Setup bidirectional relationships BEFORE saving
        testUser.addGeofence(permanentGeofence);
        testUser.addGeofence(temporaryGeofence);

        // Save user (cascade will save geofences)
        testUser = userRepository.save(testUser);
        entityManager.flush(); // ID 할당을 위해 즉시 DB 반영

        // Reload to get managed entities with IDs
        testUser = userRepository.findByNumberWithGeofences(testUser.getNumber());
        List<Geofence> geofences = testUser.getGeofences();
        permanentGeofence = geofences.stream()
                .filter(g -> "Home".equals(g.getName()))
                .findFirst()
                .orElseThrow();
        temporaryGeofence = geofences.stream()
                .filter(g -> "Temporary Location".equals(g.getName()))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("userFenceIn - permanent geofence decreases maxSequence")
    void userFenceIn_PermanentGeofence_DecreasesMaxSequence() {
        // given
        int initialMaxSequence = permanentGeofence.getMaxSequence();
        Long geofenceId = permanentGeofence.getId();

        // when
        geofenceService.userFenceIn(TEST_NUMBER, geofenceId);

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
        testUser.addGeofence(zeroSequenceGeofence);
        geofenceRepository.save(zeroSequenceGeofence);
        entityManager.flush();

        Long geofenceId = zeroSequenceGeofence.getId();

        // when
        geofenceService.userFenceIn(TEST_NUMBER, geofenceId);

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

        // when
        geofenceService.userFenceIn(TEST_NUMBER, temporaryGeofenceId);

        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(geofenceRepository.findById(temporaryGeofenceId)).isEmpty();
    }

    @Test
    @DisplayName("userFenceIn - geofence not found throws exception")
    void userFenceIn_GeofenceNotFound_ThrowsException() {
        // given
        Long nonExistentGeofenceId = 999999L;

        // when & then
        assertThatThrownBy(() -> geofenceService.userFenceIn(TEST_NUMBER, nonExistentGeofenceId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Geofence not found");
    }

    @Test
    @DisplayName("userFenceIn - multiple entries decrease maxSequence correctly")
    void userFenceIn_MultipleEntries_DecreasesCorrectly() {
        // given
        Long geofenceId = permanentGeofence.getId();
        int initialMaxSequence = permanentGeofence.getMaxSequence();

        // when - enter 3 times
        geofenceService.userFenceIn(TEST_NUMBER, geofenceId);
        geofenceService.userFenceIn(TEST_NUMBER, geofenceId);
        geofenceService.userFenceIn(TEST_NUMBER, geofenceId);

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
                "Test Location",
                "서울시 강남구 테헤란로 152",
                0,  // permanent type
                null,
                null
        );

        int initialCount = testUser.getGeofences().size();

        // when
        geofenceService.createNewFence(TEST_NUMBER, requestDto);

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
                "Temporary Event",
                "서울시 강남구 역삼동",
                1,  // temporary type
                startTime,
                endTime
        );

        int initialCount = testUser.getGeofences().size();

        // when
        geofenceService.createNewFence(TEST_NUMBER, requestDto);

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
