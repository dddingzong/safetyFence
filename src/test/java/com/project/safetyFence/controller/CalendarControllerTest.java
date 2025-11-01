package com.project.safetyFence.controller;

import com.project.safetyFence.geofence.domain.Geofence;
import com.project.safetyFence.log.domain.Log;
import com.project.safetyFence.user.domain.User;
import com.project.safetyFence.calendar.domain.UserEvent;
import com.project.safetyFence.geofence.GeofenceRepository;
import com.project.safetyFence.log.LogRepository;
import com.project.safetyFence.calendar.UserEventRepository;
import com.project.safetyFence.user.UserRepository;
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
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private GeofenceRepository geofenceRepository;

    @Autowired
    private UserEventRepository userEventRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private String testApiKey;
    private static final String TEST_NUMBER = "01012345678";

    @BeforeEach
    void setUp() {
        // Test user with API Key
        testUser = new User(TEST_NUMBER, "tester", "password", LocalDate.now(), "test-link");
        testApiKey = "test-api-key-12345678901234567890123456789012";
        testUser.updateApiKey(testApiKey);

        // 2024-10-22 데이터
        Log log1 = new Log(testUser, "집", "서울시 강남구", LocalDateTime.of(2024, 10, 22, 9, 30));
        Log log2 = new Log(testUser, "회사", "서울시 강남구 테헤란로", LocalDateTime.of(2024, 10, 22, 10, 0));

        Geofence tempGeofence1 = new Geofence(
                testUser,
                "회의실",
                "서울시 강남구",
                new BigDecimal("37.123"),
                new BigDecimal("127.123"),
                1,
                LocalDateTime.of(2024, 10, 22, 14, 0),
                LocalDateTime.of(2024, 10, 22, 16, 0),
                0
        );

        UserEvent event1 = new UserEvent(testUser, "병원 예약", LocalDate.of(2024, 10, 22), LocalTime.of(14, 30));
        UserEvent event2 = new UserEvent(testUser, "저녁 약속", LocalDate.of(2024, 10, 22), LocalTime.of(19, 0));

        // 2024-10-23 데이터
        Log log3 = new Log(testUser, "카페", "서울시 강남구 역삼동", LocalDateTime.of(2024, 10, 23, 11, 0));

        UserEvent event3 = new UserEvent(testUser, "미팅", LocalDate.of(2024, 10, 23), LocalTime.of(10, 0));

        // 2024-10-24 데이터 (UserEvent만)
        UserEvent event4 = new UserEvent(testUser, "운동", LocalDate.of(2024, 10, 24), LocalTime.of(18, 0));

        // Setup bidirectional relationships
        testUser.addLog(log1);
        testUser.addLog(log2);
        testUser.addLog(log3);
        testUser.addGeofence(tempGeofence1);
        testUser.addUserEvent(event1);
        testUser.addUserEvent(event2);
        testUser.addUserEvent(event3);
        testUser.addUserEvent(event4);

        // Save user (cascade will save all relationships)
        userRepository.save(testUser);
        entityManager.flush();
    }

    @Test
    @DisplayName("getCalendarData - 날짜별로 그룹핑된 데이터 조회 성공")
    void getCalendarData_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/calendar/userData")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(3))  // 3개의 날짜

                // 2024-10-22 검증
                .andExpect(jsonPath("$[0].date").value("2024-10-22"))
                .andExpect(jsonPath("$[0].logs.length()").value(2))
                .andExpect(jsonPath("$[0].logs[0].logId").exists())
                .andExpect(jsonPath("$[0].logs[0].location").value("집"))
                .andExpect(jsonPath("$[0].logs[0].arriveTime").value("09:30"))
                .andExpect(jsonPath("$[0].logs[1].location").value("회사"))

                .andExpect(jsonPath("$[0].geofences.length()").value(1))
                .andExpect(jsonPath("$[0].geofences[0].geofenceId").exists())
                .andExpect(jsonPath("$[0].geofences[0].name").value("회의실"))

                .andExpect(jsonPath("$[0].userEvents.length()").value(2))
                .andExpect(jsonPath("$[0].userEvents[0].event").value("병원 예약"))
                .andExpect(jsonPath("$[0].userEvents[0].eventStartTime").value("14:30"))
                .andExpect(jsonPath("$[0].userEvents[1].event").value("저녁 약속"))

                // 2024-10-23 검증
                .andExpect(jsonPath("$[1].date").value("2024-10-23"))
                .andExpect(jsonPath("$[1].logs.length()").value(1))
                .andExpect(jsonPath("$[1].logs[0].location").value("카페"))
                .andExpect(jsonPath("$[1].geofences.length()").value(0))  // 빈 배열
                .andExpect(jsonPath("$[1].userEvents.length()").value(1))
                .andExpect(jsonPath("$[1].userEvents[0].event").value("미팅"))

                // 2024-10-24 검증
                .andExpect(jsonPath("$[2].date").value("2024-10-24"))
                .andExpect(jsonPath("$[2].logs.length()").value(0))  // 빈 배열
                .andExpect(jsonPath("$[2].geofences.length()").value(0))  // 빈 배열
                .andExpect(jsonPath("$[2].userEvents.length()").value(1))
                .andExpect(jsonPath("$[2].userEvents[0].event").value("운동"))

                .andDo(print());
    }

    @Test
    @DisplayName("getCalendarData - API Key 없이 요청 시 401 에러")
    void getCalendarData_NoApiKey_Unauthorized() throws Exception {
        // when & then
        mockMvc.perform(get("/calendar/userData")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("getCalendarData - 잘못된 API Key로 요청 시 401 에러")
    void getCalendarData_InvalidApiKey_Unauthorized() throws Exception {
        // when & then
        mockMvc.perform(get("/calendar/userData")
                        .header("X-API-Key", "invalid-api-key")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("getCalendarData - 데이터가 없는 사용자도 빈 배열 반환")
    void getCalendarData_NoData_EmptyArray() throws Exception {
        // given - 데이터가 없는 새로운 사용자
        User emptyUser = new User("01099999999", "empty", "password", LocalDate.now(), "empty-link");
        String emptyApiKey = "empty-api-key-12345678901234567890123456789012";
        emptyUser.updateApiKey(emptyApiKey);
        userRepository.save(emptyUser);
        entityManager.flush();

        // when & then
        mockMvc.perform(get("/calendar/userData")
                        .header("X-API-Key", emptyApiKey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0))  // 빈 배열
                .andDo(print());
    }

    @Test
    @DisplayName("getCalendarData - 영구 Geofence는 포함되지 않음")
    void getCalendarData_PermanentGeofence_NotIncluded() throws Exception {
        // given - 영구 geofence 추가
        Geofence permanentGeofence = new Geofence(
                testUser,
                "집 영구",
                "서울시 강남구",
                new BigDecimal("37.123"),
                new BigDecimal("127.123"),
                0,  // 영구 타입
                100
        );
        testUser.addGeofence(permanentGeofence);
        geofenceRepository.save(permanentGeofence);
        entityManager.flush();

        // when & then
        mockMvc.perform(get("/calendar/userData")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2024-10-22"))
                .andExpect(jsonPath("$[0].geofences.length()").value(1))  // 영구 geofence는 포함 안됨
                .andExpect(jsonPath("$[0].geofences[0].name").value("회의실"))  // 일시적 geofence만
                .andDo(print());
    }

    @Test
    @DisplayName("getCalendarData - 날짜 순서대로 정렬 확인")
    void getCalendarData_DateSortedCorrectly() throws Exception {
        // when & then
        mockMvc.perform(get("/calendar/userData")
                        .header("X-API-Key", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2024-10-22"))
                .andExpect(jsonPath("$[1].date").value("2024-10-23"))
                .andExpect(jsonPath("$[2].date").value("2024-10-24"))
                .andDo(print());
    }
}
