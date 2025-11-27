package com.soar_be.domain.ranking.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RankingDashboardResponse {

    private Long storeId;
    private LocalDate date;
    private DashboardSummary summary;
    private List<DashboardProductItem> products;


    public static RankingDashboardResponse of(Long storeId, LocalDate date,
                                              DashboardSummary summary,
                                              List<DashboardProductItem> products) {
        return RankingDashboardResponse.builder()
                .storeId(storeId)
                .date(date)
                .summary(summary)
                .products(products)
                .build();
    }
}