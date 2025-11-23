package com.soar_be.infra.naver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverShoppingResponse {

    private String lastBuildDate;
    private Integer total;
    private Integer start;
    private Integer display;
    private List<NaverShoppingItem> items;

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
    
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }
}