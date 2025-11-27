package com.soar_be.domain.ranking.service;

import com.soar_be.domain.product.entity.Product;
import com.soar_be.domain.product.repository.ProductRepository;
import com.soar_be.domain.ranking.dto.DashboardProductItem;
import com.soar_be.domain.ranking.dto.DashboardSummary;
import com.soar_be.domain.ranking.dto.RankingDashboardResponse;
import com.soar_be.domain.ranking.dto.RankingFetchResponse;
import com.soar_be.domain.ranking.dto.RankingHistoryResponse;
import com.soar_be.domain.ranking.dto.RankingSummary;
import com.soar_be.domain.ranking.entity.ChangeTrend;
import com.soar_be.domain.ranking.entity.Ranking;
import com.soar_be.domain.ranking.repository.RankingRepository;
import com.soar_be.domain.store.entity.Store;
import com.soar_be.domain.store.repository.StoreRepository;
import com.soar_be.global.dto.ApiResponse;
import com.soar_be.global.exception.CustomException;
import com.soar_be.global.exception.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    private final RankingSearchService rankingSearchService;


    @Transactional(readOnly = true)
    public ApiResponse<RankingHistoryResponse> getRankingHistory(Long userId, Long productId,
                                                                 LocalDate startDate, LocalDate endDate,
                                                                 Integer limit) {
        Product product = findProductByIdAndValidateOwner(productId, userId);

        LocalDateTime endDateTime = (endDate != null)
                ? endDate.atTime(LocalTime.MAX)
                : LocalDateTime.now();
        LocalDateTime startDateTime = (startDate != null)
                ? startDate.atStartOfDay()
                : endDateTime.minusDays(30);

        List<Ranking> rankings;
        if (limit != null && limit > 0) {
            rankings = rankingRepository.findTop30ByProductIdOrderByCreatedAtDesc(productId);
            if (rankings.size() > limit) {
                rankings = rankings.subList(0, limit);
            }
        } else {
            rankings = rankingRepository.findByProductIdAndCreatedAtBetween(
                    productId, startDateTime, endDateTime);
        }

        RankingSummary summary = calculateRankingSummary(rankings);

        log.info("Ranking history retrieved - productId: {}, count: {}", productId, rankings.size());

        RankingHistoryResponse response = RankingHistoryResponse.from(
                productId,
                product.getActualProductName(),
                product.getPrimaryKeyword(),
                rankings,
                summary
        );

        return ApiResponse.success("순위 조회가 완료되었습니다.", response);
    }

    @Transactional(readOnly = true)
    public ApiResponse<RankingDashboardResponse> getRankingDashboard(Long userId, Long storeId,
                                                                     LocalDate date) {
        Store store = findStoreByIdAndValidateOwner(storeId, userId);

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        LocalDateTime targetDateTime = targetDate.atStartOfDay();

        List<Ranking> rankings = rankingRepository.findByStoreIdAndDate(storeId, targetDateTime);

        List<DashboardProductItem> productItems = rankings.stream()
                .map(DashboardProductItem::from)
                .toList();

        DashboardSummary summary = calculateDashboardSummary(rankings);

        log.info("Dashboard retrieved - storeId: {}, date: {}, products: {}",
                storeId, targetDate, productItems.size());

        RankingDashboardResponse response = RankingDashboardResponse.of(
                storeId, targetDate, summary, productItems);

        return ApiResponse.success("대시보드 조회가 완료되었습니다.", response);
    }


    @Transactional
    public ApiResponse<RankingFetchResponse> fetchRankingManually(Long userId, Long productId) {
        Product product = findProductByIdAndValidateOwner(productId, userId);

        Ranking ranking = rankingSearchService.searchAndSaveRanking(product);

        log.info("Manual ranking fetch completed - productId: {}, rank: {}",
                productId, ranking.getRankOverall());

        RankingFetchResponse response = RankingFetchResponse.inProgress(productId);

        return ApiResponse.success("순위 조회가 시작되었습니다.", response);
    }

    private RankingSummary calculateRankingSummary(List<Ranking> rankings) {
        if (rankings.isEmpty()) {
            return RankingSummary.builder()
                    .currentRank(null)
                    .highestRank(null)
                    .lowestRank(null)
                    .averageRank(null)
                    .trend(ChangeTrend.SAME)
                    .build();
        }

        List<Integer> visibleRanks = rankings.stream()
                .filter(Ranking::getIsVisible)
                .map(Ranking::getRankOverall)
                .filter(rank -> rank != null)
                .toList();

        if (visibleRanks.isEmpty()) {
            return RankingSummary.builder()
                    .currentRank(null)
                    .highestRank(null)
                    .lowestRank(null)
                    .averageRank(null)
                    .trend(ChangeTrend.LOST)
                    .build();
        }

        Integer currentRank = rankings.get(0).getRankOverall();
        Integer highestRank = visibleRanks.stream().min(Integer::compareTo).orElse(null);
        Integer lowestRank = visibleRanks.stream().max(Integer::compareTo).orElse(null);
        Double averageRank = visibleRanks.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        ChangeTrend trend = rankings.get(0).getChangeTrend();

        return RankingSummary.builder()
                .currentRank(currentRank)
                .highestRank(highestRank)
                .lowestRank(lowestRank)
                .averageRank(Math.round(averageRank * 10) / 10.0)
                .trend(trend)
                .build();
    }

    private DashboardSummary calculateDashboardSummary(List<Ranking> rankings) {
        int totalProducts = rankings.size();
        int upCount = 0;
        int downCount = 0;
        int sameCount = 0;
        int newCount = 0;
        int lostCount = 0;

        for (Ranking ranking : rankings) {
            switch (ranking.getChangeTrend()) {
                case UP -> upCount++;
                case DOWN -> downCount++;
                case SAME -> sameCount++;
                case NEW -> newCount++;
                case LOST -> lostCount++;
            }
        }

        return DashboardSummary.builder()
                .totalProducts(totalProducts)
                .upCount(upCount)
                .downCount(downCount)
                .sameCount(sameCount)
                .newCount(newCount)
                .lostCount(lostCount)
                .build();
    }

    private Product findProductByIdAndValidateOwner(Long productId, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.getStore().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }

        return product;
    }

    private Store findStoreByIdAndValidateOwner(Long storeId, Long userId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (!store.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }

        return store;
    }
}