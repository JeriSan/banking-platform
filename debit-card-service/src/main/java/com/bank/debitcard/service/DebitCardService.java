package com.bank.debitcard.service;

import com.bank.debitcard.domain.DebitCard;
import com.bank.debitcard.domain.DebitCardMovement;
import com.bank.debitcard.dto.AccountDto;
import com.bank.debitcard.repository.DebitCardMovementRepository;
import com.bank.debitcard.repository.DebitCardRepository;
import com.bank.debitcard.webclient.AccountClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DebitCardService {
    private final DebitCardRepository repo;
    private final DebitCardMovementRepository movRepo;
    private final AccountClient accountClient;

    public Mono<DebitCard> create(DebitCard c){
        c.setActive(true);
        if (c.getLinkedAccountIds()==null) c.setLinkedAccountIds(new ArrayList<>());
        // Garantiza que primary esté primero
        if (c.getPrimaryAccountId()!=null){
            List<String> ordered = new ArrayList<>();
            ordered.add(c.getPrimaryAccountId());
            c.getLinkedAccountIds().stream()
                    .filter(a -> !a.equals(c.getPrimaryAccountId()))
                    .forEach(ordered::add);
            c.setLinkedAccountIds(ordered);
        }
        return repo.save(c);
    }

    public Mono<BigDecimal> primaryBalance(String cardId){
        return repo.findById(cardId).flatMap(card -> accountClient.getBalance(card.getPrimaryAccountId()));
    }

    /** Pago/retiro con fallback en el orden de cuentas asociadas */
    public Mono<Void> payOrWithdraw(String cardId, BigDecimal amount, String description, String type){
        return repo.findById(cardId).flatMap(card -> {
            if (!card.isActive()) return Mono.error(new IllegalStateException("Card not active"));
            List<String> ordered = card.getLinkedAccountIds();
            if (ordered==null || ordered.isEmpty()) return Mono.error(new IllegalStateException("No linked accounts"));

            // Intento de retiro por orden
            return tryWithdrawCascade(ordered, amount, description)
                    .flatMap(usedAccountId -> {
                        DebitCardMovement m = DebitCardMovement.builder()
                                .cardId(cardId).timestamp(LocalDateTime.now()).type(type)
                                .amount(amount).accountUsedId(usedAccountId).description(description).build();
                        return movRepo.save(m).then();
                    });
        });
    }

    private Mono<String> tryWithdrawCascade(List<String> accountIds, BigDecimal amount, String note){
        // Intenta retirar completamente de una sola cuenta; si falla por saldo, pasa a la siguiente.
        return Flux.fromIterable(accountIds)
                .concatMap(accountId ->
                        accountClient.getBalance(accountId)
                                .flatMap(bal -> bal.compareTo(amount) >= 0
                                        ? accountClient.withdraw(accountId, amount, note).thenReturn(accountId)
                                        : Mono.empty()
                                )
                )
                .next()
                .switchIfEmpty(Mono.error(new IllegalStateException("Insufficient funds across linked accounts")));
    }

    public Flux<DebitCardMovement> last10Movements(String cardId){
        return movRepo.findByCardIdOrderByTimestampDesc(cardId).take(10);
    }
/*
    public Mono<DebitCard> linkAllCustomerAccounts(String cardId){
        return repo.findById(cardId).flatMap(card ->
                accountClient.getAccount(card.getPrimaryAccountId()) // para obtener customerId
                        .flatMap(primary -> accountClientListByCustomer(primary.getCustomerId())
                                .collectList()
                                .flatMap(list -> {
                                    List<String> ids = list.stream().map(AccountDto::getId).toList();
                                    List<String> ordered = new ArrayList<>();
                                    ordered.add(card.getPrimaryAccountId());
                                    ids.stream().filter(id -> !id.equals(card.getPrimaryAccountId())).forEach(ordered::add);
                                    card.setLinkedAccountIds(ordered);
                                    return repo.save(card);
                                })
                        )
        );
    }

    // Necesitas un endpoint en account-service: GET /api/accounts?customerId=...
    private Flux<AccountDto> accountClientListByCustomer(String customerId){
        return accountClientAccountsByCustomer(customerId); // implementa en AccountClient si lo agregas
    }*/
}