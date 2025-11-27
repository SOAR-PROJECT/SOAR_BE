package com.soar_be.domain.product.dto;

import com.soar_be.domain.product.entity.Product;
import com.soar_be.domain.product.entity.ProductStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProductDetailResponse {
    private Long productId;
    private Long storeId;
    private String managementCode;
    private String registeredName;
    private String actualProductName;
    private String primaryKeyword;
    private String productUrl;
    private String marketplace;
    private LocalDate registeredDate;
    private ProductStatus status;
    private List<RankingHistoryItem> rankingHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductDetailResponse from(Product product) {
        return ProductDetailResponse.builder()
                .productId(product.getId())
                .storeId(product.getStore().getId())
                .managementCode(product.getManagementCode())
                .registeredName(product.getRegisteredName())
                .actualProductName(product.getActualProductName())
                .primaryKeyword(product.getPrimaryKeyword())
                .productUrl(product.getProductUrl())
                .marketplace(product.getMarketplace())
                .registeredDate(product.getRegisteredDate())
                .status(product.getStatus())
                .rankingHistory(List.of())  // TODO: 순위 조회 기능 구현 후 연결
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RankingHistoryItem {
        private Long rankingId;
        private Integer rankOverall;
        private String changeTrend;
        private Integer previousRank;
        private Boolean isVisible;
        private LocalDateTime createdAt;
    }
}