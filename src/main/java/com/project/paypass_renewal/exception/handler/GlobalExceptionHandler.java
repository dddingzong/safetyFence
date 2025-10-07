package com.project.paypass_renewal.exception.handler;

import com.project.paypass_renewal.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException exception){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", exception.getErrorResult().getMessage());
        errorResponse.put("status", exception.getErrorResult().getHttpStatus().value());

        log.warn("CustomException 발생: {}", exception.getErrorResult().getMessage());

        return  ResponseEntity.status(exception.getErrorResult().getHttpStatus()).body(errorResponse);
    }

}
