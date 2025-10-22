package com.project.safetyFence.service;

import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.UserEvent;
import com.project.safetyFence.domain.dto.request.EventDataRequestDto;
import com.project.safetyFence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final UserRepository userRepository;

    public void addEvent(EventDataRequestDto eventDataRequestDto) {
        String number = eventDataRequestDto.getNumber();

        User user = userRepository.findByNumber(number);

        String event = eventDataRequestDto.getEvent();
        String eventDate = eventDataRequestDto.getEventDate(); // "2024-10-22" 형태
        String startTime = eventDataRequestDto.getStartTime(); // "14:30" 형태

        // 문자열을 LocalDate로 변환
        LocalDate date = LocalDate.parse(eventDate);
        LocalTime time = LocalTime.parse(startTime);

        UserEvent userEvent = new UserEvent(user, event, date, time);

        user.addUserEvent(userEvent);
        userRepository.save(user);
    }

}
