package com.soar_be.domain.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RankingFetchResponse {

    private Long productId;
    private String status;

    public static RankingFetchResponse inProgress(Long productId) {
        return RankingFetchResponse.builder()
                .productId(productId)
                .status("IN_PROGRESS")
                .build();
    }
}