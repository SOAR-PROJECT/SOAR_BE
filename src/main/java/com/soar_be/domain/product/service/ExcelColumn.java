package com.soar_be.domain.product.service;

public enum ExcelColumn {
    MANAGEMENT_CODE(0, "관리번호"),
    REGISTERED_NAME(1, "등록 상품명"),
    ACTUAL_PRODUCT_NAME(2, "상품명"),
    PRIMARY_KEYWORD(3, "키워드"),
    MARKETPLACE(4, "판매처"),
    REGISTERED_DATE(5, "등록일");

    private final int index;
    private final String displayName;

    ExcelColumn(int index, String displayName) {
        this.index = index;
        this.displayName = displayName;
    }

    public int getIndex() {
        return index;
    }

    public String getDisplayName() {
        return displayName;
    }
}
