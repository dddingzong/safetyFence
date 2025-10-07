package com.project.paypass_renewal.repository;

import com.project.paypass_renewal.domain.Log;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class LogRepositoryTest {

    @Autowired
    LogRepository logRepository;

    @Test
    @DisplayName("로그_데이터_저장_테스트")
    void saveLogTest() {
        // given
        Log log = new Log("01012345678",
                LocalDateTime.of(2025, 6, 15, 14, 30),
                LocalDateTime.of(2025, 6, 15, 14, 50),
                "집",
                "센터");

        // when
        Log savedLog = logRepository.save(log);

        // then
        assertThat(savedLog.getId()).isEqualTo(log.getId());
        assertThat(savedLog.getArrivalTime()).isEqualTo(log.getArrivalTime());
        assertThat(savedLog.getArrivalLocation()).isEqualTo(log.getArrivalLocation());
    }

    @Test
    @DisplayName("로그_데이터_조회_테스트")
    void findByNumberTest(){
        // given
        String number = "01012345678";

        Log testLogOne = new Log("01012345678", LocalDateTime.of(2025, 6, 15, 14, 30), LocalDateTime.of(2025, 6, 15, 14, 50), "12453", "42343");
        Log testLogTwo = new Log("01012345678", LocalDateTime.of(2025, 6, 15, 14, 55), LocalDateTime.of(2025, 6, 15, 14, 58), "12312", "12453");
        Log testLogThree = new Log("01011112222", LocalDateTime.of(2025, 6, 15, 14, 30), LocalDateTime.of(2025, 6, 15, 14, 50), "12453", "42343");

        logRepository.save(testLogOne);
        logRepository.save(testLogTwo);
        logRepository.save(testLogThree);

        // when
        List<Log> findNumbers = logRepository.findByNumberOrderByDepartureTimeDesc(number);

        // then
        assertThat(findNumbers).hasSize(2);
        assertThat(findNumbers).extracting("number").containsOnly("01012345678");
        assertThat(findNumbers).extracting("arrivalLocation").contains("42343", "12453");
    }

}
