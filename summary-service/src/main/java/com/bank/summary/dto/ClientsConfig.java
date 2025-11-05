package com.bank.summary.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientsConfig {
    @Bean WebClient account(@Value("${account.service.base-url}") String b){ return WebClient.builder().baseUrl(b).build(); }
    @Bean WebClient credit(@Value("${credit.service.base-url}") String b){ return WebClient.builder().baseUrl(b).build(); }
    @Bean WebClient card(@Value("${card.service.base-url}") String b){ return WebClient.builder().baseUrl(b).build(); }
    @Bean WebClient debit(@Value("${debit.service.base-url}") String b){ return WebClient.builder().baseUrl(b).build(); }
}
