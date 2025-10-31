package com.bank.credit.repository;

import com.bank.credit.domain.CreditPayment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

public interface CreditPaymentRepository extends ReactiveMongoRepository<CreditPayment, String> {

    Flux<CreditPayment> findByCreditIdOrderByTimestampDesc(String creditId);

}


