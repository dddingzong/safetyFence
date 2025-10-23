package com.project.safetyFence.controller;

import com.project.safetyFence.domain.dto.response.LogResponseDto;
import com.project.safetyFence.service.LogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @GetMapping("/logs")
    public ResponseEntity<List<LogResponseDto>> getLogs(HttpServletRequest request) {
        String userNumber = (String) request.getAttribute("userNumber");
        List<LogResponseDto> logs = logService.getLogsByUserNumber(userNumber);
        return ResponseEntity.ok(logs);
    }



}
