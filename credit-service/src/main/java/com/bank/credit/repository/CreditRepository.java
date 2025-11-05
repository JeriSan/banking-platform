package com.bank.credit.repository;

import com.bank.credit.domain.Credit;
import com.bank.credit.domain.CreditType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditRepository extends ReactiveMongoRepository<Credit, String> {

    Flux<Credit> findByCustomerId(String customerId);

    Mono<Long> countByCustomerIdAndTypeAndActiveIsTrue(String customerId, CreditType type);
    Mono<Boolean> existsByCustomerIdAndOverdueIsTrue(String customerId); //
}


