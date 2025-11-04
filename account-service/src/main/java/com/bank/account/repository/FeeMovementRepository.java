package com.bank.account.repository;

import com.bank.account.domain.FeeMovement;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public interface FeeMovementRepository extends ReactiveMongoRepository<FeeMovement, String> {
   Flux<FeeMovement> findByAccountIdAndTimestampBetween(String accountId, LocalDateTime from, LocalDateTime to);
}