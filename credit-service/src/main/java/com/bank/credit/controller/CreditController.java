package com.bank.credit.controller;

import com.bank.credit.domain.Credit;
import com.bank.credit.domain.CreditPayment;
import com.bank.credit.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
public class CreditController {
    private final CreditService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Credit> create(@RequestBody Credit credit){
        return service.create(credit);
    }

    @GetMapping
    public Flux<Credit> findAll(){
        return service.findAll();
    }

    @PutMapping("/{id}")
    public Mono<Credit> update(@PathVariable String id, @RequestBody Credit c){
        return service.update(id, c);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id){
        return service.delete(id);
    }

    @PostMapping("/{id}/pay")
    public Mono<Credit> pay(@PathVariable String id, @RequestParam BigDecimal amount){
        return service.pay(id,amount);
    }

    @GetMapping("/{id}/balance")
    public Mono<BigDecimal> balance (@PathVariable String id){
        return service.balance(id);
    }

    @GetMapping("/{id}/payments")
    public Flux<CreditPayment> payments(@PathVariable String id){
        return service.payments(id);
    }


}
