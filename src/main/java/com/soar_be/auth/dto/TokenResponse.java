package com.soar_be.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "create")
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}
