package com.project.safetyFence.controller;

import com.project.safetyFence.domain.dto.request.LinkRequestDto;
import com.project.safetyFence.domain.dto.response.LinkResponseDto;
import com.project.safetyFence.service.LinkService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @GetMapping("/link/list")
    public ResponseEntity<List<LinkResponseDto>> getUserLink(HttpServletRequest request) {
        String userNumber = (String) request.getAttribute("userNumber");
        List<LinkResponseDto> links = linkService.getUserLink(userNumber);
        return ResponseEntity.ok(links);
    }

    @PostMapping("/link/addUser")
    public ResponseEntity<String> addLinkUser(HttpServletRequest request, @RequestBody LinkRequestDto linkRequestDto) {
        String userNumber = (String) request.getAttribute("userNumber");
        linkService.addLinkUser(userNumber, linkRequestDto);
        return ResponseEntity.ok("Link user added successfully");
    }



}
