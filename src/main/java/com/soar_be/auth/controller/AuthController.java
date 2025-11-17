package com.soar_be.auth.controller;

import com.soar_be.auth.api.AuthAPI;
import com.soar_be.auth.dto.LoginRequest;
import com.soar_be.auth.dto.RefreshRequest;
import com.soar_be.auth.dto.SignupRequest;
import com.soar_be.auth.dto.TokenResponse;
import com.soar_be.auth.service.AuthService;
import com.soar_be.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthAPI {
    private final AuthService authService;

    @Override
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<TokenResponse>> signup(@RequestBody SignupRequest signupRequest) {
        ApiResponse<TokenResponse> response = authService.signup(signupRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(LoginRequest loginRequest) {
        ApiResponse<TokenResponse> response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(RefreshRequest refreshRequest) {
        ApiResponse<TokenResponse> response = authService.refresh(refreshRequest);
        return ResponseEntity.ok(response);
    }
}
