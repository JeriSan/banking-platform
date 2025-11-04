package com.bank.account.webClient;

import com.bank.account.controller.AccountController;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@lombok.RequiredArgsConstructor
public class CardClient {
    private final WebClient cardWebClient;
    public reactor.core.publisher.Mono<Boolean> hasActiveCard(String customerId){
        return cardWebClient.get()
                .uri("/api/cards/has-active/{customerId}", customerId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorReturn(false);
    }
}