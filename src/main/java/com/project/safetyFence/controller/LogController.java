package com.project.safetyFence.controller;

import com.project.safetyFence.domain.dto.request.NumberRequestDto;
import com.project.safetyFence.domain.dto.response.LogResponseDto;
import com.project.safetyFence.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @GetMapping("/logs")
    public ResponseEntity<List<LogResponseDto>> getLogs(@RequestParam NumberRequestDto numberRequestDto, @RequestHeader("Authorization") String authorization) {
        List<LogResponseDto> logs = logService.getLogsByUserNumber(numberRequestDto);
        return ResponseEntity.ok(logs);
    }



}
