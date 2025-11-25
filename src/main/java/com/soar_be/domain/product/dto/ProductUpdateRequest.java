package com.soar_be.domain.product.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    private String registeredName;

    private String actualProductName;

    private String primaryKeyword;

    private String productUrl;

    private String marketplace;

    private LocalDate registeredDate;
}