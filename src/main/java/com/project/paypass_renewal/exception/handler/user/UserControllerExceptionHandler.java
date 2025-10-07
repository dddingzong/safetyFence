package com.project.paypass_renewal.exception.handler.user;

import com.project.paypass_renewal.controller.UserController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(assignableTypes = UserController.class)
public class UserControllerExceptionHandler {

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


    // @JsonFormat 날짜 형식 오류 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJacksonParseException(HttpMessageNotReadableException exception) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "날짜 형식이 올바르지 않습니다. 2000-01-01 형식을 사용하세요.");
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.badRequest().body(errorResponse);
    }


}
