package com.project.safetyFence.controller;


import com.project.safetyFence.domain.dto.request.EventDataRequestDto;
import com.project.safetyFence.domain.dto.request.NumberRequestDto;
import com.project.safetyFence.domain.dto.response.UserDataResponseDto;
import com.project.safetyFence.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/calendar/userData")
    public ResponseEntity<List<UserDataResponseDto>> getUserData(@RequestParam NumberRequestDto numberRequestDto, @RequestHeader("Authorization") String authorization) {



        return ResponseEntity.ok(null);
    }


    @PostMapping("claendar/addEvent")
    public ResponseEntity<String> addEvent(@RequestBody EventDataRequestDto eventDataRequestDto) {
        calendarService.addEvent(eventDataRequestDto);
        return ResponseEntity.ok("Event added");
    }


}
