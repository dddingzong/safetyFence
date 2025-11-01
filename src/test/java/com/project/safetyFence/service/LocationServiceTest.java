package com.project.safetyFence.service;

import com.project.safetyFence.user.domain.User;
import com.project.safetyFence.location.domain.UserLocation;
import com.project.safetyFence.location.dto.LocationUpdateDto;
import com.project.safetyFence.location.UserLocationRepository;
import com.project.safetyFence.user.UserRepository;
import com.project.safetyFence.location.LocationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * LocationService 통합 테스트
 * 조건부 DB 저장 로직 및 Haversine 거리 계산 검증 (비동기 + 실제 DB)
 */
@SpringBootTest
@DisplayName("LocationService 단위 테스트")
class LocationServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserLocationRepository userLocationRepository;

    @Autowired
    private LocationService locationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트 전 DB 정리
        userLocationRepository.deleteAll();
        userRepository.deleteAll();

        // 실제 DB에 테스트 사용자 저장
        testUser = new User(
                "user123",
                "홍길동",
                "password",
                LocalDate.of(1990, 1, 1),
                "LINK123"
        );
        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 DB 정리
        userLocationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("첫 위치 전송 시 무조건 저장")
    void 첫_위치_전송_시_무조건_저장() {
        // Given
        LocationUpdateDto locationDto = new LocationUpdateDto(
                "user123",
                37.123456,
                127.123456,
                System.currentTimeMillis()
        );

        // When
        locationService.saveLocationIfNeeded(locationDto);

        // Then - 비동기 작업 완료 대기 후 DB 조회
        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<UserLocation> locations = userLocationRepository.findAll();
                    assertThat(locations).hasSize(1);
                    assertThat(locations.get(0).getLatitude()).isEqualByComparingTo(BigDecimal.valueOf(37.123456));
                    assertThat(locations.get(0).getLongitude()).isEqualByComparingTo(BigDecimal.valueOf(127.123456));
                });
    }

    @Test
    @DisplayName("100m 이상 이동 시 저장")
    void 거리_100m_이상_이동_시_저장() {
        // Given - 이전 위치를 먼저 저장
        UserLocation previousLocation = createUserLocation(
                testUser,
                37.5665,
                126.9780,
                LocalDateTime.now().minusSeconds(10)
        );
        testUser.addUserLocation(previousLocation);
        userLocationRepository.save(previousLocation);

        // 광화문 (37.5758, 126.9768) - 약 1km 거리
        LocationUpdateDto newLocation = new LocationUpdateDto(
                "user123",
                37.5758,
                126.9768,
                System.currentTimeMillis()
        );

        // When
        locationService.saveLocationIfNeeded(newLocation);

        // Then - 비동기 작업 완료 대기 후 DB 조회 (총 2개)
        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<UserLocation> locations = userLocationRepository.findAll();
                    assertThat(locations).hasSize(2);
                });
    }

    @Test
    @DisplayName("100m 미만 이동 시 저장 안 함")
    void 거리_100m_미만_이동_시_저장_안_함() throws Exception {
        // Given - 이전 위치를 먼저 저장
        UserLocation previousLocation = createUserLocation(
                testUser,
                37.123456,
                127.123456,
                LocalDateTime.now().minusSeconds(10)
        );
        testUser.addUserLocation(previousLocation);
        userLocationRepository.save(previousLocation);

        LocationUpdateDto newLocation = new LocationUpdateDto(
                "user123",
                37.123466, // 약 10m 이동
                127.123466,
                System.currentTimeMillis()
        );

        // When
        locationService.saveLocationIfNeeded(newLocation);

        // Then - 비동기 작업 완료 대기 후 여전히 1개만 있어야 함
        Thread.sleep(1000); // 1초 대기
        List<UserLocation> locations = userLocationRepository.findAll();
        assertThat(locations).hasSize(1);
    }

    @Test
    @DisplayName("1분 이상 경과 시 저장")
    void 시간_1분_이상_경과_시_저장() {
        // Given - 이전 위치를 먼저 저장
        UserLocation previousLocation = createUserLocation(
                testUser,
                37.123456,
                127.123456,
                LocalDateTime.now().minusMinutes(2) // 2분 전
        );
        testUser.addUserLocation(previousLocation);
        userLocationRepository.save(previousLocation);

        // 같은 위치지만 1분 이상 경과
        LocationUpdateDto newLocation = new LocationUpdateDto(
                "user123",
                37.123456,
                127.123456,
                System.currentTimeMillis()
        );

        // When
        locationService.saveLocationIfNeeded(newLocation);

        // Then - 비동기 작업 완료 대기 후 DB 조회 (총 2개)
        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<UserLocation> locations = userLocationRepository.findAll();
                    assertThat(locations).hasSize(2);
                });
    }

    @Test
    @DisplayName("1분 미만 경과 + 100m 미만 이동 시 저장 안 함")
    void 시간_1분_미만_경과_거리_100m_미만_이동_시_저장_안_함() throws Exception {
        // Given - 이전 위치를 먼저 저장
        UserLocation previousLocation = createUserLocation(
                testUser,
                37.123456,
                127.123456,
                LocalDateTime.now().minusSeconds(30) // 30초 전
        );
        testUser.addUserLocation(previousLocation);
        userLocationRepository.save(previousLocation);

        // 30초 후, 50m 이동
        LocationUpdateDto newLocation = new LocationUpdateDto(
                "user123",
                37.123906, // 약 50m 이동
                127.123456,
                System.currentTimeMillis()
        );

        // When
        locationService.saveLocationIfNeeded(newLocation);

        // Then - 비동기 작업 완료 대기 후 여전히 1개만 있어야 함
        Thread.sleep(1000); // 1초 대기
        List<UserLocation> locations = userLocationRepository.findAll();
        assertThat(locations).hasSize(1);
    }

    @Test
    @DisplayName("사용자가 없는 경우 저장 안 함")
    void 사용자가_없는_경우_저장_안_함() throws Exception {
        // Given
        LocationUpdateDto locationDto = new LocationUpdateDto(
                "nonExistent",
                37.123456,
                127.123456,
                System.currentTimeMillis()
        );

        // When
        locationService.saveLocationIfNeeded(locationDto);

        // Then - 비동기 작업 완료 대기 후 아무것도 저장 안 됨
        Thread.sleep(1000); // 1초 대기
        List<UserLocation> locations = userLocationRepository.findAll();
        assertThat(locations).isEmpty();
    }

    @Test
    @DisplayName("정확히 100m 이동 시 저장 (경계값 테스트)")
    void 정확히_100m_이동_시_저장() {
        // Given - 이전 위치를 먼저 저장
        UserLocation previousLocation = createUserLocation(
                testUser,
                37.123456,
                127.123456,
                LocalDateTime.now().minusSeconds(10)
        );
        testUser.addUserLocation(previousLocation);
        userLocationRepository.save(previousLocation);

        // 정확히 100m 이동 (위도 약 0.0009도 = 100m)
        LocationUpdateDto newLocation = new LocationUpdateDto(
                "user123",
                37.124356, // 약 100m 북쪽
                127.123456,
                System.currentTimeMillis()
        );

        // When
        locationService.saveLocationIfNeeded(newLocation);

        // Then - 비동기 작업 완료 대기 후 DB 조회 (총 2개)
        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<UserLocation> locations = userLocationRepository.findAll();
                    assertThat(locations).hasSize(2);
                });
    }

    @Test
    @DisplayName("정확히 1분 경과 시 저장 (경계값 테스트)")
    void 정확히_1분_경과_시_저장() {
        // Given - 이전 위치를 먼저 저장
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        UserLocation previousLocation = createUserLocation(
                testUser,
                37.123456,
                127.123456,
                oneMinuteAgo
        );
        testUser.addUserLocation(previousLocation);
        userLocationRepository.save(previousLocation);

        LocationUpdateDto newLocation = new LocationUpdateDto(
                "user123",
                37.123456,
                127.123456,
                System.currentTimeMillis()
        );

        // When
        locationService.saveLocationIfNeeded(newLocation);

        // Then - 비동기 작업 완료 대기 후 DB 조회 (총 2개)
        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<UserLocation> locations = userLocationRepository.findAll();
                    assertThat(locations).hasSize(2);
                });
    }

    @Test
    @DisplayName("매우 먼 거리 이동 시 저장 (1km 이상)")
    void 매우_먼_거리_이동_시_저장() {
        // Given - 이전 위치를 먼저 저장
        UserLocation previousLocation = createUserLocation(
                testUser,
                37.5665,
                126.9780,
                LocalDateTime.now().minusSeconds(10)
        );
        testUser.addUserLocation(previousLocation);
        userLocationRepository.save(previousLocation);

        // 부산 (35.1796, 129.0756) - 약 325km
        LocationUpdateDto newLocation = new LocationUpdateDto(
                "user123",
                35.1796,
                129.0756,
                System.currentTimeMillis()
        );

        // When
        locationService.saveLocationIfNeeded(newLocation);

        // Then - 비동기 작업 완료 대기 후 DB 조회 (총 2개)
        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<UserLocation> locations = userLocationRepository.findAll();
                    assertThat(locations).hasSize(2);
                });
    }

    // 테스트 헬퍼 메서드
    private UserLocation createUserLocation(User user, double lat, double lng, LocalDateTime savedTime) {
        UserLocation location = new UserLocation(
                user,
                BigDecimal.valueOf(lat),
                BigDecimal.valueOf(lng)
        );

        // 리플렉션으로 savedTime 설정
        try {
            java.lang.reflect.Field field = UserLocation.class.getDeclaredField("savedTime");
            field.setAccessible(true);
            field.set(location, savedTime);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set savedTime", e);
        }

        return location;
    }
}
