package com.project.safetyFence.controller;

import com.project.safetyFence.domain.dto.request.NumberRequestDto;
import com.project.safetyFence.domain.dto.response.LogListResponseDto;
import com.project.safetyFence.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @PostMapping("/log/getLogList")
    public ResponseEntity<List<LogListResponseDto>> getLogList(@RequestBody NumberRequestDto numberRequestDto) {

        List<LogListResponseDto> logListByNumber = logService.getLogListByNumber(numberRequestDto);

        return ResponseEntity.ok(logListByNumber);
    }


}
