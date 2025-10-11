package com.project.safetyFence.exception.handler.mypage;

import com.project.safetyFence.controller.MyPageController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(assignableTypes = MyPageController.class)
public class MyPageControllerExceptionHandler {

    // @Valid 유효성 검사 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException exception){
        Map<String, Object> errorResponse = new HashMap<>();

        String errorMessage = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("유효성 검사 실패");

        errorResponse.put("message", errorMessage);
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());

        log.warn("Validation failed: {}", errorMessage);

        return ResponseEntity.badRequest().body(errorResponse);
    }



}
