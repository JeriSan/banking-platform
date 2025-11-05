package com.bank.debitcard.webclient;

import com.bank.debitcard.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AccountClient {
    private final WebClient accountWebClient;

    public Mono<AccountDto> getAccount(String id){
        return accountWebClient.get().uri("/api/accounts/{id}", id)
                .retrieve().bodyToMono(AccountDto.class);
    }
    public Mono<BigDecimal> getBalance(String id){
        return accountWebClient.get().uri("/api/accounts/{id}/balance", id)
                .retrieve().bodyToMono(BigDecimal.class);
    }
    public Mono<AccountDto> withdraw(String id, BigDecimal amount, String note){
        return accountWebClient.post().uri(uriBuilder ->
                        uriBuilder.path("/api/accounts/{id}/withdraw")
                                .queryParam("amount", amount).queryParam("note", note).build(id))
                .retrieve().bodyToMono(AccountDto.class);
    }
}