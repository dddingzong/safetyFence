package com.project.safetyFence.service;

import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.UserEvent;
import com.project.safetyFence.domain.dto.request.EventDataRequestDto;
import com.project.safetyFence.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CalendarServiceTest {

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    private static final String TEST_NUMBER = "01012345678";

    @BeforeEach
    void setUp() {
        // Test user
        testUser = new User(TEST_NUMBER, "tester", "password", LocalDate.of(1990, 1, 1), "test-link");
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("addEvent - successfully adds event to user")
    void addEvent_Success() {
        // given
        EventDataRequestDto requestDto = new EventDataRequestDto(
                TEST_NUMBER,
                "Meeting with client",
                "2024-10-25",
                "14:30"
        );

        int initialEventCount = testUser.getUserEvents().size();

        // when
        calendarService.addEvent(requestDto);

        // then
        User updatedUser = userRepository.findByNumber(TEST_NUMBER);
        assertThat(updatedUser.getUserEvents()).hasSize(initialEventCount + 1);

        UserEvent addedEvent = updatedUser.getUserEvents().get(0);
        assertThat(addedEvent.getEvent()).isEqualTo("Meeting with client");
        assertThat(addedEvent.getEventDate()).isEqualTo(LocalDate.of(2024, 10, 25));
        assertThat(addedEvent.getStartTime().toString()).isEqualTo("14:30");
        assertThat(addedEvent.getUser()).isEqualTo(updatedUser);
    }

    @Test
    @DisplayName("addEvent - adds multiple events to same user")
    void addEvent_MultipleEvents_Success() {
        // given
        EventDataRequestDto event1 = new EventDataRequestDto(
                TEST_NUMBER,
                "Morning Meeting",
                "2024-10-25",
                "09:00"
        );

        EventDataRequestDto event2 = new EventDataRequestDto(
                TEST_NUMBER,
                "Lunch Appointment",
                "2024-10-25",
                "12:30"
        );

        EventDataRequestDto event3 = new EventDataRequestDto(
                TEST_NUMBER,
                "Evening Conference",
                "2024-10-26",
                "18:00"
        );

        // when
        calendarService.addEvent(event1);
        calendarService.addEvent(event2);
        calendarService.addEvent(event3);

        // then
        User updatedUser = userRepository.findByNumber(TEST_NUMBER);
        assertThat(updatedUser.getUserEvents()).hasSize(3);

        assertThat(updatedUser.getUserEvents())
                .extracting("event")
                .containsExactlyInAnyOrder("Morning Meeting", "Lunch Appointment", "Evening Conference");
    }

    @Test
    @DisplayName("addEvent - maintains bidirectional relationship")
    void addEvent_MaintainsBidirectionalRelationship() {
        // given
        EventDataRequestDto requestDto = new EventDataRequestDto(
                TEST_NUMBER,
                "Team Building",
                "2024-11-01",
                "10:00"
        );

        // when
        calendarService.addEvent(requestDto);

        // then
        User updatedUser = userRepository.findByNumber(TEST_NUMBER);
        UserEvent addedEvent = updatedUser.getUserEvents().get(0);

        // Verify bidirectional relationship
        assertThat(addedEvent.getUser()).isEqualTo(updatedUser);
        assertThat(updatedUser.getUserEvents()).contains(addedEvent);
    }

    @Test
    @DisplayName("addEvent - handles different date formats correctly")
    void addEvent_DifferentDates_Success() {
        // given
        EventDataRequestDto requestDto = new EventDataRequestDto(
                TEST_NUMBER,
                "New Year Event",
                "2025-01-01",
                "00:00"
        );

        // when
        calendarService.addEvent(requestDto);

        // then
        User updatedUser = userRepository.findByNumber(TEST_NUMBER);
        UserEvent addedEvent = updatedUser.getUserEvents().get(0);

        assertThat(addedEvent.getEventDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(addedEvent.getStartTime().toString()).isEqualTo("00:00");
    }

    @Test
    @DisplayName("addEvent - data persists across multiple calls")
    void addEvent_DataPersistence() {
        // given
        EventDataRequestDto requestDto = new EventDataRequestDto(
                TEST_NUMBER,
                "Persistent Event",
                "2024-12-25",
                "15:00"
        );

        // when
        calendarService.addEvent(requestDto);

        // then - first retrieval
        User user1 = userRepository.findByNumber(TEST_NUMBER);
        assertThat(user1.getUserEvents()).hasSize(1);

        // then - second retrieval (verify persistence)
        User user2 = userRepository.findByNumber(TEST_NUMBER);
        assertThat(user2.getUserEvents()).hasSize(1);
        assertThat(user2.getUserEvents().get(0).getEvent()).isEqualTo("Persistent Event");
    }
}
