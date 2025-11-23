package com.soar_be.infra.naver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverShoppingItem {

    private String title;
    private String link;
    private String image;
    private String lprice;
    private String hprice;
    private String mallName;
    private String productId;
    private String productType;
    private String brand;
    private String maker;
    private String category1;
    private String category2;
    private String category3;
    private String category4;

    public String extractProductIdFromLink() {
        if (link == null || link.isEmpty()) {
            return null;
        }

        if (link.contains("smartstore.naver.com")) {
            String[] parts = link.split("/products/");
            if (parts.length > 1) {
                String id = parts[1].split("\\?")[0];
                return id;
            }
        }

        return null;
    }
}