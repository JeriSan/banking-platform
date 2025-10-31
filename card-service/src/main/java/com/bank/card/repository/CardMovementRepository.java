package com.bank.card.repository;

import com.bank.card.domain.CardMovement;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface CardMovementRepository extends ReactiveMongoRepository<CardMovement, String> {
    Flux<CardMovement> findByCardIdOrderByTimestampDesc(String cardId);
}
