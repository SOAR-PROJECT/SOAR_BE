package com.soar_be.domain.product.dto;

import com.soar_be.domain.product.entity.Product;
import com.soar_be.domain.product.entity.ProductStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProductResponse {

    private Long productId;
    private Long storeId;
    private String managementCode;
    private String registeredName;
    private String actualProductName;
    private String primaryKeyword;
    private String marketplace;
    private LocalDate registeredDate;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .productId(product.getId())
                .storeId(product.getStore().getId())
                .managementCode(product.getManagementCode())
                .registeredName(product.getRegisteredName())
                .actualProductName(product.getActualProductName())
                .primaryKeyword(product.getPrimaryKeyword())
                .marketplace(product.getMarketplace())
                .registeredDate(product.getRegisteredDate())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}