package com.soar_be.domain.product.dto;

import com.soar_be.domain.product.entity.Product;
import com.soar_be.domain.product.entity.ProductStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
@AllArgsConstructor
public class ProductListResponse {

    private java.util.List<ProductItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static ProductListResponse from(Page<Product> productPage) {
        return ProductListResponse.builder()
                .content(productPage.getContent().stream()
                        .map(ProductItem::from)
                        .toList())
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProductItem {
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
        private LatestRank latestRank;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static ProductItem from(Product product) {
            return ProductItem.builder()
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
                    .latestRank(null)  // TODO: 순위 조회 기능 구현 후 연결
                    .createdAt(product.getCreatedAt())
                    .updatedAt(product.getUpdatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LatestRank {
        private Integer rankOverall;
        private String changeTrend;
        private Integer previousRank;
    }
}