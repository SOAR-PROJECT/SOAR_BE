package com.soar_be.auth.api;

import com.soar_be.auth.dto.SignupRequest;
import com.soar_be.auth.dto.TokenResponse;
import com.soar_be.global.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthAPI {
    ResponseEntity<ApiResponse<TokenResponse>> signup(@RequestBody SignupRequest signupRequest);
}
