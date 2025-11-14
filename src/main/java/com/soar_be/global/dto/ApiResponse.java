package com.soar_be.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String errorCode;   // 👈 추가

    private static final String SUCCESS_MSG = "요청이 성공적으로 처리되었습니다.";

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, SUCCESS_MSG, data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null);
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, errorCode);
    }
}
