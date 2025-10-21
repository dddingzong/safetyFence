package com.project.safetyFence.service;

import com.project.safetyFence.domain.Log;
import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.dto.request.NumberRequestDto;
import com.project.safetyFence.domain.dto.response.LogResponseDto;
import com.project.safetyFence.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class LogServiceTest {

    @Autowired
    private LogService logService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Log log1;
    private Log log2;
    private Log log3;

    private static final String TEST_NUMBER = "01012345678";
    private static final String TEST_NUMBER_NO_LOGS = "01099999999";

    @BeforeEach
    void setUp() {
        // Test user with logs
        testUser = new User(TEST_NUMBER, "tester", "password", LocalDate.of(1990, 1, 1), "test-link");

        // Create test logs and add to user using convenience method
        log1 = new Log(
                testUser,
                "Home",
                "Seoul Gangnam, 123 Main St",
                LocalDateTime.of(2024, 10, 21, 9, 30)
        );
        testUser.addLog(log1);

        log2 = new Log(
                testUser,
                "Office",
                "Seoul Gangdong, 456 Business Ave",
                LocalDateTime.of(2024, 10, 21, 14, 0)
        );
        testUser.addLog(log2);

        log3 = new Log(
                testUser,
                "Cafe",
                "Seoul Seocho, 789 Coffee Lane",
                LocalDateTime.of(2024, 10, 21, 16, 30)
        );
        testUser.addLog(log3);

        // Save user (cascade will save logs)
        userRepository.save(testUser);

        // Test user without logs
        User userWithoutLogs = new User(TEST_NUMBER_NO_LOGS, "no-logs-user", "password", LocalDate.of(1995, 5, 5), "test-link-2");
        userRepository.save(userWithoutLogs);
    }

    @Test
    @DisplayName("getLogsByUserNumber - returns all logs for existing user")
    void getLogsByUserNumber_ExistingUser_ReturnsAllLogs() {
        // given
        NumberRequestDto requestDto = new NumberRequestDto(TEST_NUMBER);

        // when
        List<LogResponseDto> result = logService.getLogsByUserNumber(requestDto);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("location")
                .containsExactlyInAnyOrder("Home", "Office", "Cafe");
        assertThat(result).extracting("locationAddress")
                .containsExactlyInAnyOrder(
                        "Seoul Gangnam, 123 Main St",
                        "Seoul Gangdong, 456 Business Ave",
                        "Seoul Seocho, 789 Coffee Lane"
                );
    }

    @Test
    @DisplayName("getLogsByUserNumber - returns empty list for user with no logs")
    void getLogsByUserNumber_UserWithNoLogs_ReturnsEmptyList() {
        // given
        NumberRequestDto requestDto = new NumberRequestDto(TEST_NUMBER_NO_LOGS);

        // when
        List<LogResponseDto> result = logService.getLogsByUserNumber(requestDto);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getLogsByUserNumber - response contains correct log details")
    void getLogsByUserNumber_ReturnsCorrectLogDetails() {
        // given
        NumberRequestDto requestDto = new NumberRequestDto(TEST_NUMBER);

        // when
        List<LogResponseDto> result = logService.getLogsByUserNumber(requestDto);

        // then
        LogResponseDto firstLog = result.stream()
                .filter(log -> "Home".equals(log.getLocation()))
                .findFirst()
                .orElseThrow();

        assertThat(firstLog.getId()).isNotNull();
        assertThat(firstLog.getLocation()).isEqualTo("Home");
        assertThat(firstLog.getLocationAddress()).isEqualTo("Seoul Gangnam, 123 Main St");
        assertThat(firstLog.getArriveTime()).contains("2024-10-21");
        assertThat(firstLog.getArriveTime()).contains("09:30");
    }

    @Test
    @DisplayName("getLogsByUserNumber - verify all logs have valid arrive times")
    void getLogsByUserNumber_AllLogsHaveValidArriveTimes() {
        // given
        NumberRequestDto requestDto = new NumberRequestDto(TEST_NUMBER);

        // when
        List<LogResponseDto> result = logService.getLogsByUserNumber(requestDto);

        // then
        assertThat(result).allMatch(log -> log.getArriveTime() != null && !log.getArriveTime().isEmpty());
    }

    @Test
    @DisplayName("getLogsByUserNumber - maintains data integrity after multiple calls")
    void getLogsByUserNumber_MultipleCallsMaintainDataIntegrity() {
        // given
        NumberRequestDto requestDto = new NumberRequestDto(TEST_NUMBER);

        // when - call multiple times
        List<LogResponseDto> result1 = logService.getLogsByUserNumber(requestDto);
        List<LogResponseDto> result2 = logService.getLogsByUserNumber(requestDto);

        // then - both results should be identical
        assertThat(result1).hasSize(result2.size());
        assertThat(result1).extracting("location")
                .containsExactlyInAnyOrderElementsOf(
                        result2.stream().map(LogResponseDto::getLocation).toList()
                );
    }
}
