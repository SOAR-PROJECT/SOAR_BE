package com.soar_be.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.soar_be.auth.details.CustomUserDetails;
import com.soar_be.domain.user.entity.Role;
import com.soar_be.global.exception.CustomException;
import com.soar_be.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

public class JwtTokenProviderTest {

    private final String TEST_SECRET_KEY = "abcdefghijklmnopqrstuvwxyz123456";
    private final long TEST_ACCESS_EXPIRATION = 60 * 1000L;  // 1 minute
    private final long TEST_REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7 days
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    public void setUp() {
        this.jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "SECRET_KEY", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(jwtTokenProvider, "ACCESS_TOKEN_EXPIRATION", TEST_ACCESS_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenProvider, "REFRESH_TOKEN_EXPIRATION", TEST_REFRESH_EXPIRATION);

        jwtTokenProvider.init();
    }

    @Test
    void AccessToken_생성_테스트() {
        // given
        String email = "test@gmail.com";
        Long userId = 1L;
        Role role = Role.USER;

        // when
        String accessToken = jwtTokenProvider.generateAccessToken(email, userId, role);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(TEST_SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(accessToken)
                .getBody();

        // then
        assertEquals(email, claims.getSubject());
        assertEquals(userId.intValue(), claims.get("userId", Integer.class));
        assertEquals(role.name(), claims.get("role", String.class));
    }

    @Test
    void RefreshToken_생성_테스트() {
        // given
        String email = "test@gmail.com";

        // when
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(TEST_SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        // then
        assertEquals(email, claims.getSubject());
        assertNull(claims.get("userId"));
        assertNull(claims.get("role"));
    }

    @Test
    void 토큰검증_유효하면_true_반환() {
        // given
        String token = jwtTokenProvider.generateAccessToken("test@example.com", 1L, Role.USER);

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
                .signWith(Keys.hmacShaKeyFor("another-secret-key-for-fake-token-12345".getBytes()))
                .compact();

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(fakeToken))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    void 토큰검증_만료된_토큰은_TOKEN_EXPIRED_예외발생() {
        // given
        String expiredToken = Jwts.builder()
                .setSubject("test@example.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes()))
                .compact();

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(expiredToken))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TOKEN_EXPIRED);
    }

    @Test
    void 인증객체_생성_테스트() {
        // given
        Long userId = 100L;
        String email = "user@test.com";
        Role role = Role.ADMIN;

        String token = jwtTokenProvider.generateAccessToken(email, userId, role);

        // when
        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        // then
        assertTrue(authentication.isAuthenticated());

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        assertEquals(userId, userDetails.getUserId());
        assertEquals(email, userDetails.getUsername());

        assertEquals("ROLE_" + role.name(), authentication.getAuthorities().iterator().next().getAuthority());
    }
}
