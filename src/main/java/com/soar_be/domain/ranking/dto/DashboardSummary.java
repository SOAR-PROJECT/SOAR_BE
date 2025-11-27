package com.soar_be.domain.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DashboardSummary {
    private Integer totalProducts;
    private Integer upCount;
    private Integer downCount;
    private Integer sameCount;
    private Integer newCount;
    private Integer lostCount;
}