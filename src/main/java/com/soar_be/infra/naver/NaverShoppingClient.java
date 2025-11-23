package com.soar_be.infra.naver;

import com.soar_be.infra.naver.dto.NaverShoppingItem;
import com.soar_be.infra.naver.dto.NaverShoppingResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverShoppingClient {

    private final NaverApiProperties naverApiProperties;
    private final RestTemplate restTemplate;

    private static final int DISPLAY_SIZE = 100;
    private static final int MAX_START = 1001;
    private static final String SEARCH_PATH = "/v1/search/shop.json";

    public Optional<NaverShoppingResponse> search(String keyword, int start) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = String.format("%s%s?query=%s&display=%d&start=%d&sort=sim",
                    naverApiProperties.getBaseUrl(),
                    SEARCH_PATH,
                    encodedKeyword,
                    DISPLAY_SIZE,
                    start);

            RequestEntity<Void> request = RequestEntity
                    .get(URI.create(url))
                    .header("X-Naver-Client-Id", naverApiProperties.getClientId())
                    .header("X-Naver-Client-Secret", naverApiProperties.getClientSecret())
                    .build();

            ResponseEntity<NaverShoppingResponse> response = restTemplate.exchange(
                    request,
                    NaverShoppingResponse.class
            );

            return Optional.ofNullable(response.getBody());

        } catch (RestClientException e) {
            log.error("네이버 쇼핑 API 호출 실패 - keyword: {}, start: {}, error: {}",
                    keyword, start, e.getMessage());
            return Optional.empty();
        }
    }

    public List<NaverShoppingItem> searchAll(String keyword) {
        List<NaverShoppingItem> allItems = new ArrayList<>();

        for (int start = 1; start <= MAX_START; start += DISPLAY_SIZE) {
            Optional<NaverShoppingResponse> response = search(keyword, start);

            if (response.isEmpty() || response.get().isEmpty()) {
                log.debug("검색 결과 없음 - keyword: {}, start: {}", keyword, start);
                break;
            }

            NaverShoppingResponse data = response.get();
            allItems.addAll(data.getItems());

            if (data.getItemCount() < DISPLAY_SIZE) {
                break;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("네이버 쇼핑 검색 완료 - keyword: {}, 총 {}개 결과", keyword, allItems.size());
        return allItems;
    }

    public Optional<Integer> findRankByProductUrl(String keyword, String productUrl) {
        List<NaverShoppingItem> items = searchAll(keyword);

        for (int i = 0; i < items.size(); i++) {
            NaverShoppingItem item = items.get(i);
            if (item.getLink() != null && item.getLink().contains(extractProductId(productUrl))) {
                int rank = i + 1;
                log.debug("상품 순위 발견 - keyword: {}, rank: {}", keyword, rank);
                return Optional.of(rank);
            }
        }

        log.debug("상품 미노출 - keyword: {}, productUrl: {}", keyword, productUrl);
        return Optional.empty();
    }

    public Optional<Integer> findRankByMallAndProductId(String keyword, String mallName, String productId) {
        List<NaverShoppingItem> items = searchAll(keyword);

        for (int i = 0; i < items.size(); i++) {
            NaverShoppingItem item = items.get(i);

            boolean mallMatch = mallName != null && mallName.equals(item.getMallName());
            boolean productMatch = productId != null &&
                    item.getLink() != null &&
                    item.getLink().contains(productId);

            if (mallMatch && productMatch) {
                int rank = i + 1;
                log.debug("상품 순위 발견 - keyword: {}, mallName: {}, rank: {}", keyword, mallName, rank);
                return Optional.of(rank);
            }
        }

        log.debug("상품 미노출 - keyword: {}, mallName: {}, productId: {}", keyword, mallName, productId);
        return Optional.empty();
    }

    private String extractProductId(String productUrl) {
        if (productUrl == null || productUrl.isEmpty()) {
            return "";
        }

        if (productUrl.contains("/products/")) {
            String[] parts = productUrl.split("/products/");
            if (parts.length > 1) {
                return parts[1].split("\\?")[0];
            }
        }

        return productUrl;
    }
}