package com.soar_be.domain.ranking.service;

import com.soar_be.domain.product.entity.Product;
import com.soar_be.domain.ranking.entity.ChangeTrend;
import com.soar_be.domain.ranking.entity.Ranking;
import com.soar_be.domain.ranking.repository.RankingRepository;
import com.soar_be.infra.naver.NaverShoppingClient;
import com.soar_be.infra.naver.dto.NaverShoppingItem;
import com.soar_be.infra.naver.dto.NaverShoppingResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingSearchService {

    private static final int MAX_RANK = 1000;
    private static final int PAGE_SIZE = 100;

    private final NaverShoppingClient naverShoppingClient;
    private final RankingRepository rankingRepository;


    @Transactional
    public Ranking searchAndSaveRanking(Product product) {
        String keyword = product.getPrimaryKeyword();
        String managementCode = product.getManagementCode();
        String storeName = product.getStore().getName();

        log.info("Starting rank search - productId: {}, keyword: {}", product.getId(), keyword);
        log.info("Product details - managementCode: {}, store: {}", managementCode, storeName);

        if (managementCode == null || managementCode.isBlank()) {
            log.warn("Management code is null or empty for productId: {}", product.getId());
            return saveRankingAsNotFound(product);
        }

        Integer foundRank = null;

        for (int start = 1; start <= MAX_RANK; start += PAGE_SIZE) {
            if (start > 1) {
                try {
                    Thread.sleep(200);  // 200ms 대기
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Sleep interrupted", e);
                }
            }

            NaverShoppingResponse response = naverShoppingClient.search(keyword, PAGE_SIZE, start);

            if (response == null || response.getItems() == null) {
                log.warn("Empty response at start: {}", start);
                continue;
            }

            log.debug("Page response - start: {}, total: {}, items: {}",
                    start, response.getTotal(), response.getItems().size());

            Optional<Integer> matchedIndex = findMatchingProduct(response.getItems(), managementCode);

            if (matchedIndex.isPresent()) {
                foundRank = start + matchedIndex.get();
                log.info("Product found - productId: {}, rank: {}", product.getId(), foundRank);
                break;
            }

            if (response.getTotal() == 0 || response.getItems().isEmpty()) {
                log.info("No more results after start: {}", start);
                break;
            }
        }

        Optional<Ranking> previousRanking = rankingRepository.findTopByProductIdOrderByCreatedAtDesc(product.getId());

        ChangeTrend changeTrend = calculateChangeTrend(foundRank, previousRanking);
        Integer previousRank = previousRanking.map(Ranking::getRankOverall).orElse(null);

        Ranking ranking = Ranking.builder()
                .product(product)
                .rankOverall(foundRank)
                .rankPage(foundRank != null ? (foundRank - 1) / PAGE_SIZE + 1 : null)
                .isVisible(foundRank != null)
                .changeTrend(changeTrend)
                .previousRank(previousRank)
                .build();

        Ranking savedRanking = rankingRepository.save(ranking);

        log.info("Ranking saved - productId: {}, rank: {}, trend: {}",
                product.getId(), foundRank, changeTrend);

        return savedRanking;
    }

    /**
     * API 응답의 상품 목록에서 관리번호(productId)로 매칭
     */
    private Optional<Integer> findMatchingProduct(java.util.List<NaverShoppingItem> items,
                                                  String managementCode) {
        if (managementCode == null || managementCode.isBlank()) {
            log.warn("Management code is null or empty, cannot match product");
            return Optional.empty();
        }

        for (int i = 0; i < items.size(); i++) {
            NaverShoppingItem item = items.get(i);

            if (managementCode.equals(item.getProductId())) {
                log.debug("Product matched by productId - index: {}, productId: {}, mallName: {}",
                        i, managementCode, item.getMallName());
                return Optional.of(i);
            }
        }

        return Optional.empty();
    }

    /**
     * 상품을 찾지 못한 경우 미노출로 저장
     */
    private Ranking saveRankingAsNotFound(Product product) {
        Optional<Ranking> previousRanking = rankingRepository.findTopByProductIdOrderByCreatedAtDesc(product.getId());

        Ranking ranking = Ranking.builder()
                .product(product)
                .rankOverall(null)
                .rankPage(null)
                .isVisible(false)
                .changeTrend(ChangeTrend.LOST)
                .previousRank(previousRanking.map(Ranking::getRankOverall).orElse(null))
                .build();

        return rankingRepository.save(ranking);
    }

    private ChangeTrend calculateChangeTrend(Integer currentRank, Optional<Ranking> previousRanking) {
        if (previousRanking.isEmpty()) {
            return currentRank != null ? ChangeTrend.NEW : ChangeTrend.LOST;
        }

        Integer previousRank = previousRanking.get().getRankOverall();
        Boolean wasPreviousVisible = previousRanking.get().getIsVisible();

        if (currentRank == null) {
            return wasPreviousVisible ? ChangeTrend.LOST : ChangeTrend.LOST;
        }

        if (previousRank == null || !wasPreviousVisible) {
            return ChangeTrend.NEW;
        }

        if (currentRank < previousRank) {
            return ChangeTrend.UP;
        } else if (currentRank > previousRank) {
            return ChangeTrend.DOWN;
        } else {
            return ChangeTrend.SAME;
        }
    }
}