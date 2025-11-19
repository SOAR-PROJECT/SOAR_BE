package com.soar_be.domain.store.controller;

import com.soar_be.auth.details.CustomUserDetails;
import com.soar_be.domain.store.api.StoreAPI;
import com.soar_be.domain.store.dto.StoreRequest;
import com.soar_be.domain.store.dto.StoreResponse;
import com.soar_be.domain.store.service.StoreService;
import com.soar_be.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
