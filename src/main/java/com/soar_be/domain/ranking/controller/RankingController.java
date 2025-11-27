package com.soar_be.domain.ranking.controller;

import com.soar_be.auth.details.CustomUserDetails;
import com.soar_be.domain.ranking.api.RankingAPI;
import com.soar_be.domain.ranking.dto.RankingDashboardResponse;
import com.soar_be.domain.ranking.dto.RankingFetchResponse;
import com.soar_be.domain.ranking.dto.RankingHistoryResponse;
import com.soar_be.domain.ranking.service.RankingService;
import com.soar_be.global.dto.ApiResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RankingController implements RankingAPI {

    private final RankingService rankingService;

    @Override
    @GetMapping("/products/{productId}/rankings")
    public ResponseEntity<ApiResponse<RankingHistoryResponse>> getRankingHistory(
            CustomUserDetails userDetails,
            @PathVariable("productId") Long productId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value = "limit", required = false, defaultValue = "30") Integer limit) {

        log.debug("Ranking history request - userId: {}, productId: {}, startDate: {}, endDate: {}, limit: {}",
                userDetails.getUserId(), productId, startDate, endDate, limit);

        ApiResponse<RankingHistoryResponse> response = rankingService.getRankingHistory(
                userDetails.getUserId(),
                productId,
                startDate,
                endDate,
                limit
        );

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/stores/{storeId}/rankings/dashboard")
    public ResponseEntity<ApiResponse<RankingDashboardResponse>> getRankingDashboard(
            CustomUserDetails userDetails,
            @PathVariable("storeId") Long storeId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        log.debug("Dashboard request - userId: {}, storeId: {}, date: {}",
                userDetails.getUserId(), storeId, date);

        ApiResponse<RankingDashboardResponse> response = rankingService.getRankingDashboard(
                userDetails.getUserId(),
                storeId,
                date
        );

        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/products/{productId}/rankings/fetch")
    public ResponseEntity<ApiResponse<RankingFetchResponse>> fetchRankingManually(
            CustomUserDetails userDetails,
            @PathVariable("productId") Long productId) {

        log.debug("Manual ranking fetch request - userId: {}, productId: {}",
                userDetails.getUserId(), productId);

        ApiResponse<RankingFetchResponse> response = rankingService.fetchRankingManually(
                userDetails.getUserId(),
                productId
        );

        return ResponseEntity.ok(response);
    }
}