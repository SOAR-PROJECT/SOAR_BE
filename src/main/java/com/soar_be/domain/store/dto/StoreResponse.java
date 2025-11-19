package com.soar_be.domain.store.dto;

import com.soar_be.domain.store.entity.Store;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreResponse {

    private Long storeId;
    private Long userId;
    private String name;
    private Integer productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StoreResponse from(Store store) {
        return StoreResponse.builder()
                .storeId(store.getId())
                .userId(store.getUser().getId())
                .name(store.getName())
                .productCount(store.getProducts() != null ? store.getProducts().size() : 0)
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
    
}
