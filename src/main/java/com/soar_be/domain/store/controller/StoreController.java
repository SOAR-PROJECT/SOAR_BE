package com.soar_be.domain.store.controller;

import com.soar_be.auth.details.CustomUserDetails;
import com.soar_be.domain.store.api.StoreAPI;
import com.soar_be.domain.store.dto.StoreRequest;
import com.soar_be.domain.store.dto.StoreResponse;
import com.soar_be.domain.store.service.StoreService;
import com.soar_be.global.dto.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController implements StoreAPI {

    private final StoreService storeService;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(CustomUserDetails userDetails,
                                                                  StoreRequest storeRequest) {
        log.debug("Store creation request - userId: {}, storeName: {}",
                userDetails.getUserId(), storeRequest.getName());

        ApiResponse<StoreResponse> response = storeService.createStore(
                userDetails.getUserId(),
                storeRequest
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getMyStores(CustomUserDetails userDetails) {
        log.debug("Store list request - userId: {}", userDetails.getUserId());

        ApiResponse<List<StoreResponse>> response = storeService.getMyStores(userDetails.getUserId());

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStoresById(Long storeId, CustomUserDetails userDetails) {
        log.debug("Store request - userId: {}, storeId: {}", userDetails.getUserId(), storeId);

        ApiResponse<StoreResponse> response = storeService.getStoreById(storeId, userDetails.getUserId());

        return ResponseEntity.ok(response);
    }
}
