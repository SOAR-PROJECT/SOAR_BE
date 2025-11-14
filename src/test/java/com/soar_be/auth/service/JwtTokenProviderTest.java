package com.soar_be.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.soar_be.global.exception.CustomException;
import com.soar_be.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class JwtTokenProviderTest {
    private final String TEST_SECRETE_KEY = "abcdefghijklmnopqrstuvwxyz123456";
    private final long TEST_EXPIRATION = 1000L * 60;
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    public void setUp() {
        this.jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "SECRET_KEY", TEST_SECRETE_KEY);
        ReflectionTestUtils.setField(jwtTokenProvider, "EXPIRATION", TEST_EXPIRATION);

        jwtTokenProvider.init();
    }

    @Test
    void 토큰생성_테스트() {
        // given
        String email = "test@gmail.com";
        Long userId = 1L;

        // when
        String jwtToken = jwtTokenProvider.generateToken(email, userId);

        Claims claims = Jwts.parserBuilder().
                setSigningKey(TEST_SECRETE_KEY.getBytes()).
                build().parseClaimsJws(jwtToken).getBody();

        // then
        assertEquals(email, claims.getSubject());
        assertEquals(userId.intValue(), claims.get("userId", Integer.class));
    }

    @Test
    void 토큰검증_테스트_유효하면_true_반환() {
        // given
        String token = jwtTokenProvider.generateToken("test@example.com", 1L);

        // when
        boolean result = jwtTokenProvider.validateToken(token);

        // then
        assertTrue(result);
    }

    @Test
    void 토큰검증_예외_테스트_위조된_토큰이면_INVALID_TOKEN_예외를_던짐() {
        // given
        String token = jwtTokenProvider.generateToken("test@example.com", 1L);
        String fakeToken = Jwts.builder()
                .setSubject("test@example.com")
                .claim("userId", 1L)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TEST_EXPIRATION))
                .signWith(Keys.hmacShaKeyFor("another-secrete-key-for-fake-token-12345".getBytes()))
                .compact();

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(fakeToken))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    void 토큰검증_예외_테스트_만료된_토큰이면_EXPIRED_TOKEN_예외를_던짐() {
        // given
        String expiredToken = Jwts.builder()
                .setSubject("test@example.com")
                .claim("userId", 1L)
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 5000)) // 이미 만료됨
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(TEST_SECRETE_KEY.getBytes()))
                .compact();

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(expiredToken))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TOKEN_EXPIRED);
    }
}
