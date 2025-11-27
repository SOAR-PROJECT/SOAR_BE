package com.soar_be.domain.ranking.dto;

import com.soar_be.domain.ranking.entity.ChangeTrend;
import com.soar_be.domain.ranking.entity.Ranking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DashboardProductItem {

    private Long productId;
    private String managementCode;
    private String registeredName;
    private String keyword;
    private Integer currentRank;
    private Integer previousRank;
    private ChangeTrend changeTrend;
    private Integer changeAmount;
    private Boolean isVisible;


    public static DashboardProductItem from(Ranking ranking) {
        Integer changeAmount = null;
        if (ranking.getPreviousRank() != null && ranking.getRankOverall() != null) {
            changeAmount = ranking.getPreviousRank() - ranking.getRankOverall();
        }

        return DashboardProductItem.builder()
                .productId(ranking.getProduct().getId())
                .managementCode(ranking.getProduct().getManagementCode())
                .registeredName(ranking.getProduct().getRegisteredName())
                .keyword(ranking.getProduct().getPrimaryKeyword())
                .currentRank(ranking.getRankOverall())
                .previousRank(ranking.getPreviousRank())
                .changeTrend(ranking.getChangeTrend())
                .changeAmount(changeAmount)
                .isVisible(ranking.getIsVisible())
                .build();
    }
}