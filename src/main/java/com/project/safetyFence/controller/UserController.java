package com.project.safetyFence.controller;

import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.dto.request.SignInRequestDto;
import com.project.safetyFence.domain.dto.request.SignUpRequestDto;
import com.project.safetyFence.exception.CustomException;
import com.project.safetyFence.exception.ErrorResult;
import com.project.safetyFence.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/user/signIn")
    public ResponseEntity<String> signIn(@RequestBody SignInRequestDto signInRequestDto) {
        String number = signInRequestDto.getNumber();
        String password = signInRequestDto.getPassword();

        if (!userService.checkExistNumber(number)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("존재하지 않는 번호입니다.");
        }

        User user = userService.findByNumber(number);
        String userPassword = user.getPassword();

        if (!userPassword.equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 일치하지 않습니다.");
        }

        return ResponseEntity.ok("login success");
    }

    @PostMapping("/user/signup")
    public ResponseEntity<Map<String, Object>> signUp(@RequestBody SignUpRequestDto signUpRequestDto) {

        log.info("사용자 신규 회원가입 요청");

        if (userService.checkDuplicateNumber(signUpRequestDto.getNumber())) {
            throw new CustomException(ErrorResult.USER_NUMBER_DUPLICATE);
        }

        User user = userService.registerUser(signUpRequestDto);

        Map<String, Object> response = new HashMap<>();
        response.put("name", user.getName());
        response.put("number", user.getNumber());

        log.info("사용자 이름: " + user.getName() + ", 성공적으로 회원가입 완료되었습니다.");

        return ResponseEntity.ok(response);
    }

}
