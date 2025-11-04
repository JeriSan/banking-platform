package com.bank.card.service;

import com.bank.card.domain.CardMovement;
import com.bank.card.domain.CreditCard;
import com.bank.card.repository.CardMovementRepository;
import com.bank.card.repository.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CreditCardRepository repo;
    private final CardMovementRepository movRepo;

    public Mono<CreditCard> create(CreditCard c){
        c.setActive(true);
        c.setCurrentBalance(c.getCurrentBalance()==null? BigDecimal.ZERO : c.getCurrentBalance());
        return repo.save(c);
    }

    public Flux<CreditCard> findAll(){ return repo.findAll(); }
    public Mono<CreditCard> update(String id, CreditCard c){
        return repo.findById(id).switchIfEmpty(Mono.error(new IllegalArgumentException("Not found")))
                .flatMap(db -> { c.setId(id); return repo.save(c); });
    }
    public Mono<Void> delete(String id){ return repo.deleteById(id); }

    public Mono<CreditCard> charge(String cardId, BigDecimal amount, String description){
        return repo.findById(cardId).flatMap(card -> {
            if (!card.isActive()) return Mono.error(new IllegalStateException("Card not active"));
            BigDecimal available = card.getCreditLimit().subtract(card.getCurrentBalance());
            if (available.compareTo(amount) < 0) return Mono.error(new IllegalStateException("Insufficient credit limit"));
            card.setCurrentBalance(card.getCurrentBalance().add(amount));
            CardMovement m = CardMovement.builder()
                    .cardId(card.getId()).timestamp(LocalDateTime.now()).type("CHARGE").amount(amount)
                    .balanceAfter(card.getCurrentBalance()).description(description).build();
            return repo.save(card).then(movRepo.save(m)).thenReturn(card);
        });
    }

    public Mono<CreditCard> pay(String cardId, BigDecimal amount){
        return repo.findById(cardId).flatMap(card -> {
            BigDecimal newBal = card.getCurrentBalance().subtract(amount);
            card.setCurrentBalance(newBal.max(BigDecimal.ZERO));
            CardMovement m = CardMovement.builder()
                    .cardId(card.getId()).timestamp(LocalDateTime.now()).type("PAYMENT").amount(amount)
                    .balanceAfter(card.getCurrentBalance()).description("Payment").build();
            return repo.save(card).then(movRepo.save(m)).thenReturn(card);
        });
    }

    public Mono<BigDecimal> available(String cardId){
        return repo.findById(cardId).map(c -> c.getCreditLimit().subtract(c.getCurrentBalance()));
    }

    public Flux<CardMovement> movements(String cardId){
        return movRepo.findByCardIdOrderByTimestampDesc(cardId);
    }

    public reactor.core.publisher.Mono<Boolean> hasActiveCard(String customerId){
        return repo.existsByCustomerIdAndActiveIsTrue(customerId);
    }
}
