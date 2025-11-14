package com.soar_be.global.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CustomException(final ErrorCode errorCode, final String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
