package com.bank.card.controller;

import com.bank.card.domain.CardMovement;
import com.bank.card.domain.CreditCard;
import com.bank.card.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CreditCard> create(@RequestBody CreditCard c){ return service.create(c); }

    @GetMapping public Flux<CreditCard> findAll(){ return service.findAll(); }

    @PutMapping("/{id}") public Mono<CreditCard> update(@PathVariable String id, @RequestBody CreditCard c){

        return service.update(id, c);

    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)

    public Mono<Void> delete(@PathVariable String id){ return service.delete(id); }

    @PostMapping("/{id}/charge") public Mono<CreditCard> charge(@PathVariable String id, @RequestParam BigDecimal amount,

                                                                @RequestParam String description){

        return service.charge(id, amount, description);

    }

    @PostMapping("/{id}/pay") public Mono<CreditCard> pay(@PathVariable String id, @RequestParam BigDecimal amount){

        return service.pay(id, amount);

    }

    @GetMapping("/{id}/available") public Mono<BigDecimal> available(@PathVariable String id){

        return service.available(id);

    }

    @GetMapping("/{id}/movements") public Flux<CardMovement> movements(@PathVariable String id){

        return service.movements(id);

    }

}
