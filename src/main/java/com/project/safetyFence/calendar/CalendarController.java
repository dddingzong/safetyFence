package com.project.safetyFence.calendar;

import com.project.safetyFence.calendar.dto.EventDataRequestDto;
import com.project.safetyFence.calendar.dto.OneDayResponseDto;
import com.project.safetyFence.calendar.CalendarService;
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
    public ResponseEntity<List<OneDayResponseDto>> getCalendarData(HttpServletRequest request) {
        String userNumber = (String) request.getAttribute("userNumber");
        List<OneDayResponseDto> calendarData = calendarService.getCalendarData(userNumber);
        return ResponseEntity.ok(calendarData);
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
