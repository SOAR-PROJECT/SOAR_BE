package com.soar_be.domain.ranking.dto;

import com.soar_be.domain.ranking.entity.ChangeTrend;
import com.soar_be.domain.ranking.entity.Ranking;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RankingResponse {

    private Long rankingId;
    private Integer rankOverall;
    private Integer rankPage;
    private Boolean isVisible;
    private ChangeTrend changeTrend;
    private Integer previousRank;
    private LocalDateTime createdAt;

    public static RankingResponse from(Ranking ranking) {
        return RankingResponse.builder()
                .rankingId(ranking.getId())
                .rankOverall(ranking.getRankOverall())
                .rankPage(ranking.getRankPage())
                .isVisible(ranking.getIsVisible())
                .changeTrend(ranking.getChangeTrend())
                .previousRank(ranking.getPreviousRank())
                .createdAt(ranking.getCreatedAt())
                .build();
    }
}