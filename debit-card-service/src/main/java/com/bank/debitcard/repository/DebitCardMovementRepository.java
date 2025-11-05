package com.bank.debitcard.repository;

import com.bank.debitcard.domain.DebitCard;
import com.bank.debitcard.domain.DebitCardMovement;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface DebitCardMovementRepository extends ReactiveMongoRepository<DebitCardMovement, String> {
    Flux<DebitCardMovement> findByCardIdOrderByTimestampDesc(String cardId);
}