package com.soar_be.domain.ranking.dto;

import com.soar_be.domain.ranking.entity.Ranking;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RankingHistoryResponse {
    private Long productId;
    private String productName;
    private String keyword;
    private List<RankingResponse> rankings;
    private RankingSummary summary;

    public static RankingHistoryResponse from(Long productId, String productName,
                                              String keyword, List<Ranking> rankings,
                                              RankingSummary summary) {
        return RankingHistoryResponse.builder()
                .productId(productId)
                .productName(productName)
                .keyword(keyword)
                .rankings(rankings.stream()
                        .map(RankingResponse::from)
                        .toList())
                .summary(summary)
                .build();
    }
}