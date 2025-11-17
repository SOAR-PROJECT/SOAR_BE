package com.soar_be.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.soar_be.domain.user.entity.Role;
import com.soar_be.global.exception.CustomException;
import com.soar_be.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {

    private final String TEST_SECRET_KEY = "abcdefghijklmnopqrstuvwxyz12345678901234";
    private final long TEST_ACCESS_EXPIRATION = 60 * 1000L;  // 1 minute
    private final long TEST_REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7 days

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "SECRET_KEY", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(jwtTokenProvider, "ACCESS_TOKEN_EXPIRATION", TEST_ACCESS_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenProvider, "REFRESH_TOKEN_EXPIRATION", TEST_REFRESH_EXPIRATION);
        jwtTokenProvider.init();
    }

    @Test
    void TokenResponse_생성_테스트() {
        // given
        String email = "test@example.com";
        Long userId = 1L;
        Role role = Role.USER;

        // when
        var tokenResponse = jwtTokenProvider.generateTokenResponse(email, userId, role);

        // then
        assertNotNull(tokenResponse.getAccessToken());
        assertNotNull(tokenResponse.getRefreshToken());

        Claims accessClaims = Jwts.parserBuilder()
                .setSigningKey(TEST_SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(tokenResponse.getAccessToken())
                .getBody();

        Claims refreshClaims = Jwts.parserBuilder()
                .setSigningKey(TEST_SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(tokenResponse.getRefreshToken())
                .getBody();

        assertEquals(email, accessClaims.getSubject());
        assertEquals(userId, accessClaims.get("userId", Long.class));
        assertEquals(role.name(), accessClaims.get("role", String.class));

        assertEquals(email, refreshClaims.getSubject());
        assertEquals(userId, refreshClaims.get("userId", Long.class));
        assertEquals(role.name(), refreshClaims.get("role", String.class));
    }

    @Test
    void 토큰검증_유효하면_true_반환() {
        // given
        String token = jwtTokenProvider.generateTokenResponse("test@example.com", 1L, Role.USER).getAccessToken();

        // when
        boolean result = jwtTokenProvider.validateToken(token);

        // then
        assertTrue(result);
    }

    @Test
    void 토큰검증_위조된_토큰이면_INVALID_TOKEN_예외발생() {
        // given
        String fakeToken = Jwts.builder()
                .setSubject("test@example.com")
                .claim("userId", 1L)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TEST_ACCESS_EXPIRATION))
                .signWith(Keys.hmacShaKeyFor("another-secret-key-9876543210__sufficiently_long__".getBytes()))
                .compact();

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(fakeToken))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    void 토큰검증_만료된_토큰이면_TOKEN_EXPIRED_예외발생() {
        // given
        String expiredToken = Jwts.builder()
                .setSubject("test@example.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 10_000))
                .setExpiration(new Date(System.currentTimeMillis() - 5_000))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes()))
                .compact();

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(expiredToken))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TOKEN_EXPIRED);
    }
}
