package com.soar_be.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private long expiresIn;

    public static TokenResponse create(String accessToken, long expiresIn) {
        return new TokenResponse(accessToken, expiresIn);
    }
}
