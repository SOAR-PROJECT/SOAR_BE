package com.soar_be.domain.store.service;

import com.soar_be.domain.store.dto.StoreRequest;
import com.soar_be.domain.store.dto.StoreResponse;
import com.soar_be.domain.store.entity.Store;
import com.soar_be.domain.store.repository.StoreRepository;
import com.soar_be.domain.user.entity.User;
import com.soar_be.domain.user.repository.UserRepository;
import com.soar_be.global.dto.ApiResponse;
import com.soar_be.global.exception.CustomException;
import com.soar_be.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

    private static final String STORE_CREATED_SUCCESS = "스토어가 등록되었습니다.";

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Transactional
    public ApiResponse<StoreResponse> createStore(Long userId, StoreRequest storeRequest) {
        validateDuplicateStoreName(storeRequest.getName());

        User user = findUserById(userId);

        Store store = Store.builder()
                .name(storeRequest.getName())
                .user(user)
                .build();

        storeRepository.save(store);

        log.info("Store created - userId: {}, storeId: {}, storeName: {}",
                userId, store.getId(), store.getName());

        StoreResponse response = StoreResponse.from(store);
        return ApiResponse.success(STORE_CREATED_SUCCESS, response);
    }

    private void validateDuplicateStoreName(String name) {
        if (storeRepository.existsByName(name)) {
            throw new CustomException(ErrorCode.STORE_ALREADY_EXISTS);
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    }
}
