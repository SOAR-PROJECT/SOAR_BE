package com.soar_be.domain.store.api;

import com.soar_be.auth.details.CustomUserDetails;
import com.soar_be.domain.store.dto.StoreRequest;
import com.soar_be.domain.store.dto.StoreResponse;
import com.soar_be.global.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface StoreAPI {
    ResponseEntity<ApiResponse<StoreResponse>> createStore(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                           @Valid @RequestBody StoreRequest storeRequest);

    ResponseEntity<ApiResponse<List<StoreResponse>>> getMyStores(
            @AuthenticationPrincipal CustomUserDetails userDetails);

    ResponseEntity<ApiResponse<StoreResponse>> getStoresById(@PathVariable Long storeId,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails);
}
