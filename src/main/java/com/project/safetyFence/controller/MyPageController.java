package com.project.safetyFence.controller;


import com.project.safetyFence.domain.dto.response.UserDataResponseDto;
import com.project.safetyFence.service.MyPageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/get/myPageData")
    public ResponseEntity<UserDataResponseDto> getMyPageData(HttpServletRequest request) {
        String number = (String) request.getAttribute("userNumber");
        UserDataResponseDto userData = myPageService.getUserData(number);
        return ResponseEntity.ok().body(userData);
    }




}
