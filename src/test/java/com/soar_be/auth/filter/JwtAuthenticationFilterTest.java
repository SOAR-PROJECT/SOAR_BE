package com.soar_be.auth.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.soar_be.auth.service.JwtTokenProvider;
import com.soar_be.global.exception.CustomException;
import com.soar_be.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class JwtAuthenticationFilterTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();

        SecurityContextHolder.clearContext();
    }

    @Test
    void 유효한_토큰이면_SecurityContext에_Authentication_저장됨() throws Exception {
        // given
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("user", null, null);

        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getAuthentication(anyString())).thenReturn(authentication);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo("user");
    }

    @Test
    void 유효하지_않은_토큰이면_exceptionAttribute에_INVALID_TOKEN_저장됨() throws Exception {
        // given
        String token = "invalid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenProvider.validateToken(anyString()))
                .thenThrow(new CustomException(ErrorCode.INVALID_TOKEN));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Object exceptionAttr = request.getAttribute("exception");
        assertThat(exceptionAttr).isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    void 만료된_토큰이면_exceptionAttribute에_TOKEN_EXPIRED_저장됨() throws Exception {
        // given
        String token = "expired.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenProvider.validateToken(anyString()))
                .thenThrow(new CustomException(ErrorCode.TOKEN_EXPIRED));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Object exceptionAttr = request.getAttribute("exception");
        assertThat(exceptionAttr).isEqualTo(ErrorCode.TOKEN_EXPIRED);
    }

    @Test
    void Authorization_헤더없으면_아무일도_일어나지_않음() throws Exception {
        // given: no Authorization header
        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(request.getAttribute("exception")).isNull();
    }

    @Test
    void Bearer_없으면_토큰_파싱되지_않고_필터_통과() throws Exception {
        // given
        request.addHeader("Authorization", "SomethingElse token");

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
