package com.bank.account.repository;

import com.bank.account.domain.Account;
import com.bank.account.domain.AccountType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveMongoRepository<Account,String> {
    Flux<Account> findByCustomerId(String customerId);
    Flux<Account> findByBusinessTrueAndType(AccountType type);
    Mono<Long> countByCustomerIdAndType(String customerId, AccountType type);
}
