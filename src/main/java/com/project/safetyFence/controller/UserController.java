package com.project.safetyFence.controller;

import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.dto.SignInRequestDto;
import com.project.safetyFence.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/user/signIn")
    public ResponseEntity<String> signin(@RequestBody SignInRequestDto signInRequestDto) {
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







}
