package com.bank.debitcard.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    WebClient accountWebClient(@Value("${account.service.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build(); // p.ej. http://account-service:8082
    }
}