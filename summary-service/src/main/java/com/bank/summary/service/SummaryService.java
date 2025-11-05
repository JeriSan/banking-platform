package com.bank.summary.service;

import com.bank.summary.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SummaryService {
    private final WebClient account, credit, card, debit;

    public Mono<CustomerSummary> consolidated(String customerId){
        Mono<Boolean> overdue = credit.get().uri("/api/credits/overdue/{cid}", customerId)
                .retrieve().bodyToMono(Boolean.class).defaultIfEmpty(false);

        Mono<List<AccountDto>> accounts = account.get().uri(uri -> uri.path("/api/accounts").queryParam("customerId", customerId).build())
                .retrieve().bodyToFlux(AccountDto.class).collectList();

        Mono<List<CreditDto>> credits = credit.get().uri(uri -> uri.path("/api/credits").queryParam("customerId", customerId).build())
                .retrieve().bodyToFlux(CreditDto.class).collectList();

        Mono<List<CreditCardDto>> creditCards = card.get().uri(uri -> uri.path("/api/cards").queryParam("customerId", customerId).build())
                .retrieve().bodyToFlux(CreditCardDto.class).collectList();

        Mono<List<DebitCardDto>> debitCards = debit.get().uri(uri -> uri.path("/api/debit-cards").queryParam("customerId", customerId).build())
                .retrieve().bodyToFlux(DebitCardDto.class).collectList();

        return Mono.zip(overdue, accounts, credits, creditCards, debitCards)
                .map(t -> CustomerSummary.builder()
                        .customerId(customerId)
                        .hasOverdueDebt(t.getT1())
                        .accounts(t.getT2())
                        .credits(t.getT3())
                        .creditCards(t.getT4())
                        .debitCards(t.getT5())
                        .build());
    }

    // Reporte general/por producto en rango: delega a cada MS usando endpoints existentes
    public Mono<Object> productReport(String type, String from, String to){
        switch (type){
            case "ACCOUNTS" -> { // usar /api/accounts/reports/fees y/o movimientos por rango (si los expones)
                return account.get().uri(uri -> uri.path("/api/accounts/reports/fees-range")
                                .queryParam("from", from).queryParam("to", to).build())
                        .retrieve().bodyToMono(Object.class);
            }
            case "CREDITS" -> {
                return credit.get().uri(uri -> uri.path("/api/credits/payments/range")
                                .queryParam("from", from).queryParam("to", to).build())
                        .retrieve().bodyToMono(Object.class);
            }
            case "CARDS" -> {
                return card.get().uri(uri -> uri.path("/api/cards/movements/range")
                                .queryParam("from", from).queryParam("to", to).build())
                        .retrieve().bodyToMono(Object.class);
            }
            case "DEBIT_CARDS" -> {
                return debit.get().uri(uri -> uri.path("/api/debit-cards/movements/range")
                                .queryParam("from", from).queryParam("to", to).build())
                        .retrieve().bodyToMono(Object.class);
            }
            default -> { return Mono.error(new IllegalArgumentException("Unknown type")); }
        }
    }
}