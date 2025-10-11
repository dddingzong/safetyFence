package com.project.safetyFence.controller;

import com.project.safetyFence.domain.dto.request.CenterAddressRequestDto;
import com.project.safetyFence.domain.dto.request.HomeAddressRequestDto;
import com.project.safetyFence.domain.dto.request.NumberRequestDto;
import com.project.safetyFence.domain.dto.response.MyPageResponseDto;
import com.project.safetyFence.service.MyPageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @PostMapping("/myPage/getInformation")
    public ResponseEntity<MyPageResponseDto> getMyPageData(@RequestBody NumberRequestDto numberRequestDto) {

        MyPageResponseDto myPageResponseDto = myPageService.getMyPageData(numberRequestDto);

        return ResponseEntity.ok(myPageResponseDto);
    }

    @PostMapping("/myPage/updateHomeAddress")
    public ResponseEntity<String> updateHomeAddress(@Valid @RequestBody HomeAddressRequestDto homeAddressRequestDto) {

        myPageService.updateHomeAddress(homeAddressRequestDto);

        return ResponseEntity.ok("updateSuccess");
    }

    @PostMapping("/myPage/updateCenterAddress")
    public ResponseEntity<String> updateCenterAddress(@Valid @RequestBody CenterAddressRequestDto centerAddressRequestDto) {

        myPageService.updateCenterAddress(centerAddressRequestDto);

        return ResponseEntity.ok("updateSuccess");
    }

}
