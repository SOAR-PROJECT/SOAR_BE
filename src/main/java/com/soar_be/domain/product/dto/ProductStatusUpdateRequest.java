package com.soar_be.domain.product.dto;

import com.soar_be.domain.product.entity.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatusUpdateRequest {

    @NotNull(message = "상품 상태는 필수입니다.")
    private ProductStatus status;
}