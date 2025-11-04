package com.bank.card.repository;

import com.bank.card.domain.CardOwnerType;
import com.bank.card.domain.CreditCard;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditCardRepository extends ReactiveMongoRepository<CreditCard,String> {
    Flux<CreditCard> findByCustomerId(String customerId);
    Mono<Long> countByCustomerIdAndOwnerTypeAndActiveIsTrue(String customerId, CardOwnerType ownerType);
    reactor.core.publisher.Mono<Boolean> existsByCustomerIdAndActiveIsTrue(String customerId);
}
