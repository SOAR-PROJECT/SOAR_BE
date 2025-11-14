package com.soar_be.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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
}
