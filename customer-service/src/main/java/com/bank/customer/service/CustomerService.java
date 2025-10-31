package com.bank.customer.service;

import com.bank.customer.domain.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.bank.customer.repository.CustomerRepository;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepo;

    public Mono<Customer> create(Customer customer) {
        return customerRepo.findByDocumentTypeAndDocumentNumber(customer.getDocumentType(), customer.getDocumentNumber())
                .flatMap(existing -> Mono.error(new IllegalArgumentException("Customer already exists")))
                .switchIfEmpty(customerRepo.save(customer)).cast(Customer.class);
    }
    public Flux<Customer> findAll() { return customerRepo.findAll(); }

    public Mono<Customer> update(String id, Customer customer) {
        return customerRepo.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Not found")))
                .flatMap(db -> {
                    customer.setId(db.getId());
                    return customerRepo.save(customer);
                });
    }
    public Mono<Void> delete(String id) { return customerRepo.deleteById(id); }
}
