package com.soar_be.domain.product.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExcelUploadResponse {
    private Long storeId;
    private Integer totalRows;
    private Integer successCount;
    private Integer failCount;
    private List<ExcelUploadFailedRow> failedRows;

    public String getSuccessMessage() {
        if (failCount == 0) {
            return String.format("%d개 상품이 모두 등록되었습니다.", successCount);
        }
        return String.format("%d개 상품 중 %d개가 등록되었습니다.", totalRows, successCount);
    }
}
