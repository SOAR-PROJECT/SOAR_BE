package com.soar_be.domain.ranking.dto;

import com.soar_be.domain.ranking.entity.ChangeTrend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RankingSummary {
    private Integer currentRank;
    private Integer highestRank;
    private Integer lowestRank;
    private Double averageRank;
    private ChangeTrend trend;
}