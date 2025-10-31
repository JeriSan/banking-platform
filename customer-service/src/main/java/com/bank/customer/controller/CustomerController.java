package com.bank.customer.controller;

import com.bank.customer.domain.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.bank.customer.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController{
    private final CustomerService service;

    @PostMapping @ResponseStatus (HttpStatus.CREATED)
    public Mono<Customer> save(@RequestBody Customer customer) {
        return service.create(customer);
    }

    @GetMapping
    public Flux<Customer> findAll() {
        return service.findAll();
    }

    @PutMapping({"/{id}"})
    public Mono<Customer> update(@PathVariable String id, @RequestBody Customer customer) {
        return service.update(id, customer);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteById(@PathVariable String id) {
        return service.delete(id);
    }
}
