package com.soar_be.infra.naver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soar_be.infra.naver.dto.NaverShoppingResponse;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class NaverShoppingClient {

    private final RestTemplate restTemplate;
    private final String clientId;
    private final String clientSecret;
    private final String apiUrl;

    public NaverShoppingClient(
            RestTemplate restTemplate,
            @Value("${naver.api.client-id}") String clientId,
            @Value("${naver.api.client-secret}") String clientSecret,
            @Value("${naver.api.shopping-url}") String apiUrl) {
        this.restTemplate = restTemplate;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.apiUrl = apiUrl;
    }

    public NaverShoppingResponse search(String query, int display, int start) {
        log.warn("🔍 Received query parameter: [{}], length: {}", query, query.length());

        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("query", query)
                .queryParam("display", display)
                .queryParam("exclude", "used:rental:cbshop")
                .queryParam("start", start)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        headers.set("User-Agent", "Mozilla/5.0");
        headers.set("Accept-Encoding", "gzip");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<byte[]> rawResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );

            log.warn("🚨 Naver API Request URL: {}", url);

            HttpHeaders respHeaders = rawResponse.getHeaders();
            String contentEncoding = respHeaders.getFirst("Content-Encoding");
            String contentType = respHeaders.getFirst("Content-Type");
            byte[] bodyBytes = rawResponse.getBody();

            log.warn("📦 Response headers - Content-Type: {}, Content-Encoding: {}",
                    contentType, contentEncoding);
            log.warn("📦 Response body length: {}", bodyBytes != null ? bodyBytes.length : 0);

            if (bodyBytes == null || bodyBytes.length == 0) {
                log.error("Empty body from Naver API");
                throw new RuntimeException("네이버 쇼핑 API 응답이 비어 있습니다.");
            }

            String bodyString;
            if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
                try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bodyBytes))) {
                    byte[] uncompressed = gis.readAllBytes();
                    bodyString = new String(uncompressed, StandardCharsets.UTF_8);
                }
            } else {
                bodyString = new String(bodyBytes, StandardCharsets.UTF_8);
            }

            String preview = bodyString.length() > 1000
                    ? bodyString.substring(0, 1000) + "...(truncated)"
                    : bodyString;
            log.warn("🔎 Naver API RAW response text (start={}): {}", start, preview);

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(bodyString, NaverShoppingResponse.class);

        } catch (Exception e) {
            log.error("Naver Shopping API call failed - query: {}, start: {}", query, start, e);
            throw new RuntimeException("네이버 쇼핑 API 호출에 실패했습니다.", e);
        }
    }
}
