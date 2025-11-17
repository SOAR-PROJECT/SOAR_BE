package com.soar_be.auth.service;

import com.soar_be.auth.details.CustomUserDetails;
import com.soar_be.auth.dto.LoginRequest;
import com.soar_be.auth.dto.RefreshRequest;
import com.soar_be.auth.dto.SignupRequest;
import com.soar_be.auth.dto.TokenResponse;
import com.soar_be.domain.user.entity.Role;
import com.soar_be.domain.user.entity.User;
import com.soar_be.domain.user.repository.UserRepository;
import com.soar_be.global.dto.ApiResponse;
import com.soar_be.global.exception.CustomException;
import com.soar_be.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String SIGN_UP_SUCCESS = "회원가입이 완료되었습니다.";
    private static final String LOGIN_SUCCESS = "로그인이 완료되었습니다.";
    private static final String REFRESH_SUCCESS = "토큰 재발급이 완료되었습니다.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public ApiResponse<TokenResponse> signup(SignupRequest request) {

        validateEmailNonExist(request.getEmail());

        User user = makeNewUser(
                request.getEmail(),
                request.getPassword(),
                request.getName()
        );

        TokenResponse tokenResponse = jwtTokenProvider.generateTokenResponse(
                user.getEmail(),
                user.getId(),
                user.getRole()
        );

        return ApiResponse.success(SIGN_UP_SUCCESS, tokenResponse);
    }

    @Transactional(readOnly = true)
    public ApiResponse<TokenResponse> login(LoginRequest request) {
        try {
            CustomUserDetails userDetails = validateLoginInfo(request.getEmail(), request.getPassword());
            TokenResponse tokenResponse = jwtTokenProvider.generateTokenResponse(request.getEmail(),
                    userDetails.getUserId(), getRole(userDetails));
            return ApiResponse.success(LOGIN_SUCCESS, tokenResponse);
        } catch (BadCredentialsException e) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }
    }

    @Transactional
    public ApiResponse<TokenResponse> refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        validateRefreshToken(refreshToken);

        TokenResponse tokenResponse = jwtTokenProvider.generateTokenResponse(
                jwtTokenProvider.getEmailFromToken(refreshToken),
                jwtTokenProvider.getUserIdFromToken(refreshToken), jwtTokenProvider.getRoleFromToken(refreshToken));

        return ApiResponse.success(REFRESH_SUCCESS, tokenResponse);
    }

    private void validateRefreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    private CustomUserDetails validateLoginInfo(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return (CustomUserDetails) authentication.getPrincipal();
    }

    private Role getRole(CustomUserDetails userDetails) {
        return Role.valueOf(userDetails.getAuthorities().iterator().next().getAuthority().substring(5));
    }


    private void validateEmailNonExist(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    private User makeNewUser(String email, String password, String name) {
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .name(name)
                .role(Role.USER)
                .build();

        userRepository.save(user);
        return user;
    }
}
