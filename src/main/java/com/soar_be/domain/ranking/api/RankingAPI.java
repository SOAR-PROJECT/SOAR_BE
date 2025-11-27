package com.soar_be.domain.ranking.api;

import com.soar_be.auth.details.CustomUserDetails;
import com.soar_be.domain.ranking.dto.RankingDashboardResponse;
import com.soar_be.domain.ranking.dto.RankingFetchResponse;
import com.soar_be.domain.ranking.dto.RankingHistoryResponse;
import com.soar_be.global.dto.ApiResponse;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public interface RankingAPI {

    ResponseEntity<ApiResponse<RankingHistoryResponse>> getRankingHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("productId") Long productId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value = "limit", required = false, defaultValue = "30") Integer limit);

    ResponseEntity<ApiResponse<RankingDashboardResponse>> getRankingDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("storeId") Long storeId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date);

    ResponseEntity<ApiResponse<RankingFetchResponse>> fetchRankingManually(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("productId") Long productId);
}