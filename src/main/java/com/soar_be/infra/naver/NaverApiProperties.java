package com.soar_be.infra.naver;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "naver.api")
@Getter
@Setter
public class NaverApiProperties {

    private String clientId;
    private String clientSecret;
    private String baseUrl;
}