package com.soar_be.domain.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotNull(message = "스토어 ID는 필수입니다.")
    private Long storeId;

    @NotBlank(message = "관리번호는 필수입니다.")
    private String managementCode;

    @NotBlank(message = "등록 상품명은 필수입니다.")
    private String registeredName;

    @NotBlank(message = "상품명은 필수입니다.")
    private String actualProductName;

    @NotBlank(message = "키워드는 필수입니다.")
    private String primaryKeyword;

    @NotBlank(message = "판매처는 필수입니다.")
    private String marketplace;

    private LocalDate registeredDate;
}