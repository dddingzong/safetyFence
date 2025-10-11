package com.project.safetyFence.controller;

import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.dto.request.LoginRequestDto;
import com.project.safetyFence.domain.dto.request.UserRequestDto;
import com.project.safetyFence.exception.CustomException;
import com.project.safetyFence.exception.ErrorResult;
import com.project.safetyFence.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login/newUser")
    public ResponseEntity<Map<String, Object>> createNewUser(@Valid @RequestBody UserRequestDto userRequestDto) {

        log.info("사용자 신규 회원가입 요청");

        if (userService.checkDuplicateNumber(userRequestDto.getNumber())) {
            throw new CustomException(ErrorResult.USER_NUMBER_DUPLICATE);
        }

        User user = userService.saveNewUser(userRequestDto);

        Map<String, Object> response = new HashMap<>();
        response.put("name", user.getName());
        response.put("number", user.getNumber());

        log.info("사용자 이름: " + user.getName() + ", 성공적으로 회원가입 완료되었습니다.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/signIn")
    public ResponseEntity<Map<String, Object>> userSignIn(@RequestBody LoginRequestDto loginRequestDto) {

        log.info(loginRequestDto.getNumber() + " 사용자 로그인 요청");

        if (!userService.checkExistNumber(loginRequestDto.getNumber())) {
           throw new CustomException(ErrorResult.NOT_EXIST_NUMBER);
        }

        if (!userService.matchPassword(loginRequestDto)) {
            throw new CustomException(ErrorResult.USER_NOT_MATCH_PASSWORD);
        }

        log.info(loginRequestDto.getNumber() + " 로그인 완료");

        Map<String, Object> response = new HashMap<>();
        response.put("message","loginSuccess");
        response.put("number", loginRequestDto.getNumber());

        return ResponseEntity.ok(response);
    }

}
