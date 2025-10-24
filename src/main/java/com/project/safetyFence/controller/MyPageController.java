package com.project.safetyFence.controller;


import com.project.safetyFence.domain.dto.request.CenterAddressUpdateRequestDto;
import com.project.safetyFence.domain.dto.request.HomeAddressUpdateRequestDto;
import com.project.safetyFence.domain.dto.request.PasswordUpdateRequestDto;
import com.project.safetyFence.domain.dto.response.UserDataResponseDto;
import com.project.safetyFence.service.MyPageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/mypage/password")
    public ResponseEntity<String> updatePassword(@RequestBody PasswordUpdateRequestDto dto,
                                                 HttpServletRequest request) {
        String userNumber = (String) request.getAttribute("userNumber");
        myPageService.updatePassword(userNumber, dto);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }

    @PatchMapping("/mypage/homeAddress")
    public ResponseEntity<String> updateHomeAddress(@RequestBody HomeAddressUpdateRequestDto dto,
                                                    HttpServletRequest request) {
        String userNumber = (String) request.getAttribute("userNumber");
        myPageService.updateHomeAddress(userNumber, dto);
        return ResponseEntity.ok("집주소가 성공적으로 변경되었습니다.");
    }

    @PatchMapping("/mypage/centerAddress")
    public ResponseEntity<String> updateCenterAddress(@RequestBody CenterAddressUpdateRequestDto dto,
                                                      HttpServletRequest request) {
        String userNumber = (String) request.getAttribute("userNumber");
        myPageService.updateCenterAddress(userNumber, dto);
        return ResponseEntity.ok("센터주소가 성공적으로 변경되었습니다.");
    }

}
