package com.bank.customer.repository;

import com.bank.customer.domain.Customer;
import com.bank.customer.domain.CustomerType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Repository
public interface CustomerRepository extends ReactiveMongoRepository<Customer,String> {
    Mono<Customer> findByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);
    Flux<Customer> findByType(CustomerType type);
    Flux<Customer> findByActiveTrue();
}
