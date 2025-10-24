package com.project.safetyFence.controller;


import com.project.safetyFence.domain.dto.request.EventDataRequestDto;
import com.project.safetyFence.domain.dto.response.UserDataResponseDto;
import com.project.safetyFence.service.CalendarService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/calendar/userData")
    public ResponseEntity<List<UserDataResponseDto>> getUserData(HttpServletRequest request) {
        String userNumber = (String) request.getAttribute("userNumber");
        // TODO: CalendarService에서 실제 데이터 조회 구현 필요
        return ResponseEntity.ok(null);
    }

    @PostMapping("/calendar/addEvent")
    public ResponseEntity<String> addEvent(@RequestBody EventDataRequestDto eventDataRequestDto,
                                          HttpServletRequest request) {
        String userNumber = (String) request.getAttribute("userNumber");
        calendarService.addEvent(userNumber, eventDataRequestDto);
        return ResponseEntity.ok("Event added");
    }

    @DeleteMapping("/calendar/deleteEvent")
    public ResponseEntity<String> deleteEvent(HttpServletRequest request, @RequestParam Long eventId) {
        String userNumber = (String) request.getAttribute("userNumber");
        calendarService.deleteEvent(userNumber, eventId);
        return ResponseEntity.ok("Event deleted");
    }


}
