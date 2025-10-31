package com.bank.account.repository;

import com.bank.account.domain.AccountMovement;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface AccountMovementRepository extends ReactiveMongoRepository<AccountMovement, String> {
    Flux<AccountMovement> findByAccountIdOrderByTimestampDesc(String accountId);
}
