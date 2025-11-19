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
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {
    private static final String STORE_CREATED_SUCCESS = "스토어가 등록되었습니다.";
    private static final String STORE_LIST_SUCCESS = "스토어 목록 조회가 완료되었습니다.";
    private static final String STORE_DETAIL_SUCCESS = "스토어 조회가 완료되었습니다.";
    private static final String STORE_UPDATED_SUCCESS = "스토어 정보가 수정되었습니다.";
    private static final String STORE_DELETED_SUCCESS = "스토어가 삭제되었습니다.";

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

    @Transactional(readOnly = true)
    public ApiResponse<List<StoreResponse>> getMyStores(Long userId) {
        List<Store> stores = storeRepository.findAllByUserId(userId);

        List<StoreResponse> storeResponses = stores.stream()
                .map(StoreResponse::from)
                .toList();

        log.info("Fetched {} stores for userId: {}", storeResponses.size(), userId);
        return ApiResponse.success(STORE_LIST_SUCCESS, storeResponses);
    }

    @Transactional(readOnly = true)
    public ApiResponse<StoreResponse> getStoreById(Long storeId, Long userId) {
        Store store = findStoreByIdAndValidateOwner(storeId, userId);

        log.info("Fetched store - storeId: {}, userId: {}", storeId, userId);

        StoreResponse response = StoreResponse.from(store);
        return ApiResponse.success(STORE_DETAIL_SUCCESS, response);
    }

    @Transactional
    public ApiResponse<StoreResponse> updateStore(Long storeId, Long userId, StoreRequest request) {
        Store store = findStoreByIdAndValidateOwner(storeId, userId);

        if (!store.getName().equals(request.getName())) {
            validateDuplicateStoreName(request.getName());
        }

        store.updateName(request.getName());

        log.info("Store updated - storeId: {}, userId: {}, newName: {}",
                storeId, userId, store.getName());

        StoreResponse response = StoreResponse.from(store);
        return ApiResponse.success(STORE_UPDATED_SUCCESS, response);
    }

    @Transactional
    public ApiResponse<Void> deleteStore(Long storeId, Long userId) {
        Store store = findStoreByIdAndValidateOwner(storeId, userId);

        storeRepository.delete(store);

        log.info("Store deleted - storeId: {}, userId: {}", storeId, userId);

        return ApiResponse.success(STORE_DELETED_SUCCESS, null);
    }

    private Store findStoreByIdAndValidateOwner(Long storeId, Long userId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (!store.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }

        return store;
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
