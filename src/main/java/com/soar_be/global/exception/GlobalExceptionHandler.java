package com.soar_be.global.exception;

import com.soar_be.global.dto.ApiResponse;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String CUSTOM_EXCEPTION_LOG_HEADER = "CustomException: {}";
    private static final String VALIDATION_EXCEPTION_LOG_HEADER = "ValidationException: {}";
    private static final String GENERAL_EXCEPTION_LOG_HEADER = "Unexpected error: {}";

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException exception) {
        log.error(CUSTOM_EXCEPTION_LOG_HEADER, exception.getMessage(), exception);
        return ResponseEntity.status(exception.getErrorCode().getHttpStatus())
                .body(ApiResponse.error(exception.getMessage(), exception.getErrorCode().name()));

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error(VALIDATION_EXCEPTION_LOG_HEADER, message);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(message, ErrorCode.INVALID_INPUT.name()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception exception) {
        log.error(GENERAL_EXCEPTION_LOG_HEADER, exception.getMessage(), exception);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                        ErrorCode.INTERNAL_SERVER_ERROR.name()));

    }
}

