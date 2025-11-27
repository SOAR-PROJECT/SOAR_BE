package com.soar_be.config;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(
                (request, body, execution) -> {
                    System.out.println("=== Actual Request ===");
                    System.out.println("URI: " + request.getURI());
                    System.out.println("Headers: " + request.getHeaders());
                    System.out.println("Body: " + new String(body, StandardCharsets.UTF_8));
                    return execution.execute(request, body);
                }
        ));
        return restTemplate;
    }
}