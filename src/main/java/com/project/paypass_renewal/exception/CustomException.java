package com.project.paypass_renewal.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{

    private final ErrorResult errorResult;

    public CustomException(ErrorResult errorResult) {
        super(errorResult.getMessage());
        this.errorResult = errorResult;
    }

}
