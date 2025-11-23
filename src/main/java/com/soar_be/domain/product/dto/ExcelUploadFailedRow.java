package com.soar_be.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ExcelUploadFailedRow {
    private Integer row;
    private String managementCode;
    private String reason;
}
