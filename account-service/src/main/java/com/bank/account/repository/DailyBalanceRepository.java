package com.bank.account.repository;

import com.bank.account.domain.DailyBalance;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public interface DailyBalanceRepository extends ReactiveMongoRepository<DailyBalance, String> {
    Flux<DailyBalance> findByProductIdAndDateBetween(String productId, LocalDateTime from, LocalDateTime to);
}
