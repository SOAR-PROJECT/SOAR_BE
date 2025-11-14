package com.soar_be.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private final long TEST_EXPIRATION = 1000L * 60;
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    public void setUp() {
        this.jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "SECRET_KEY", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(jwtTokenProvider, "EXPIRATION", TEST_EXPIRATION);

        jwtTokenProvider.init();
    }

    @Test
    void 토큰생성_테스트() {
        // given
        String email = "test@gmail.com";
        Long userId = 1L;
        Role role = Role.USER;

        // when
        String jwtToken = jwtTokenProvider.generateToken(email, userId, role);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(TEST_SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();

        // then
        assertEquals(email, claims.getSubject());
        assertEquals(userId.intValue(), claims.get("userId", Integer.class));
        assertEquals(role.name(), claims.get("role", String.class));
    }

    @Test
    void 토큰검증_테스트_유효하면_true_반환() {
        // given
        String token = jwtTokenProvider.generateToken("test@example.com", 1L, Role.USER);

        // when
        boolean result = jwtTokenProvider.validateToken(token);

        // then
        assertTrue(result);
    }

    @Test
    void 토큰검증_예외_테스트_위조된_토큰이면_INVALID_TOKEN_예외를_던짐() {
        // given
        String validToken = jwtTokenProvider.generateToken("test@example.com", 1L, Role.USER);

        String fakeToken = Jwts.builder()
                .setSubject("test@example.com")
                .claim("userId", 1L)
                .claim("role", "USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TEST_EXPIRATION))
                .signWith(Keys.hmacShaKeyFor("another-secret-key-for-fake-token-12345".getBytes()))
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
                .claim("role", "USER")
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 5000)) // 이미 만료됨
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes()))
                .compact();

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(expiredToken))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TOKEN_EXPIRED);
    }

    @Test
    void 토큰정보_추출테스트_userID() {
        // given
        String email = "test@example.com";
        Long userId = 99L;

        String token = jwtTokenProvider.generateToken(email, userId, Role.USER);

        // when
        Long extractedId = jwtTokenProvider.getUserIdFromToken(token);

        // then
        assertEquals(userId, extractedId);
    }

    @Test
    void 토큰정보_추출테스트_email() {
        // given
        String email = "test@example.com";
        Long userId = 99L;

        String token = jwtTokenProvider.generateToken(email, userId, Role.USER);

        // when
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        // then
        assertEquals(email, extractedEmail);
    }

    @Test
    void 토큰정보_추출테스트_role() {
        // given
        Role role = Role.ADMIN;
        String token = jwtTokenProvider.generateToken("admin@abc.com", 10L, role);

        // when
        Role extractedRole = jwtTokenProvider.getRoleFromToken(token);

        // then
        assertEquals(role, extractedRole);
    }

    @Test
    void 인증객체_생성테스트() {
        // given
        Long userId = 100L;
        String email = "user@test.com";
        Role role = Role.ADMIN;

        String token = jwtTokenProvider.generateToken(email, userId, role);

        // when
        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        // then
        assertTrue(authentication.isAuthenticated());

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        assertEquals(userId, userDetails.getUserId());
        assertEquals(email, userDetails.getUsername());

        assertEquals(1, authentication.getAuthorities().size());
        assertEquals(
                "ROLE_" + role.name(),
                authentication.getAuthorities().iterator().next().getAuthority()
        );
    }
}
