package com.bank.debitcard.controller;

import com.bank.account.domain.Account;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.AccountService;
import com.bank.debitcard.domain.DebitCard;
import com.bank.debitcard.domain.DebitCardMovement;
import com.bank.debitcard.service.DebitCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController @RequestMapping("/api/debit-cards") @RequiredArgsConstructor
public class DebitCardController {
    private final DebitCardService service;
    private final AccountRepository repo;

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Mono<DebitCard> create(@RequestBody DebitCard c){ return service.create(c); }

    @PostMapping("/{id}/pay")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> pay(@PathVariable String id, @RequestParam BigDecimal amount,
                          @RequestParam(required = false) String description){
        return service.payOrWithdraw(id, amount, description, "PAYMENT");
    }

    @PostMapping("/{id}/withdraw")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> withdraw(@PathVariable String id, @RequestParam BigDecimal amount,
                               @RequestParam(required = false) String description){
        return service.payOrWithdraw(id, amount, description, "WITHDRAW");
    }

    @GetMapping("/{id}/movements/top10")
    public Flux<DebitCardMovement> last10(@PathVariable String id){
        return service.last10Movements(id);
    }

    @GetMapping("/{id}/primary-balance")
    public Mono<BigDecimal> primaryBalance(@PathVariable String id){
        return service.primaryBalance(id);
    }
/*
    @PostMapping("/{id}/link-all")
    public Mono<DebitCard> linkAll(@PathVariable String id){
        return service.linkAllCustomerAccounts(id);
    }*/

    @GetMapping(params = "customerId")
    public Flux<Account> listByCustomer(@RequestParam String customerId){
        return repo.findByCustomerId(customerId);
    }
}
