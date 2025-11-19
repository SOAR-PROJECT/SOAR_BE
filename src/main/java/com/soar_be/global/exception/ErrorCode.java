package com.soar_be.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 잘못되었습니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),

    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 잘못되었습니다."),

    STORE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 스토어가 있습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "스토어를 찾을 수 없습니다."),
    STORE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "스토어 소유자가 아닙니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
