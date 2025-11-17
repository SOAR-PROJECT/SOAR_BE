package com.soar_be.auth.service;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String SIGN_UP_SUCCESS = "회원가입이 완료되었습니다.";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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
