package com.bank.account.webClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean(name = "cardWebClient")
    public WebClient cardWebClient(
            @Value("${card.service.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl) // ej: http://card-service:8084 o http://localhost:8084
                .build();
    }

    @Bean(name = "creditWebClient")
    public WebClient creditWebClient(
            @Value("${credit.service.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
