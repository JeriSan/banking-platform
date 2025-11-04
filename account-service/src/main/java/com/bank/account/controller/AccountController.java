package com.bank.account.controller;

import com.bank.account.domain.Account;
import com.bank.account.domain.AccountMovement;
import com.bank.account.dto.AverageBalanceResponse;
import com.bank.account.dto.FeesReportResponse;
import com.bank.account.dto.TransferRequest;
import com.bank.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService service;

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Mono<Account> create(@RequestBody Account a){
        log.info("Create Account endpoint invoked");
        return service.create(a);
    }

    @GetMapping public Flux<Account> findAll(){ return service.findAll(); }

    @PutMapping("/{id}") public Mono<Account> update(@PathVariable String id, @RequestBody Account a){

        return service.update(id, a);

    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)

    public Mono<Void> delete(@PathVariable String id){ return service.delete(id); }

    // Operations

    @PostMapping("/{id}/deposit")

    public Mono<Account> deposit(@PathVariable String id, @RequestParam BigDecimal amount,

                                 @RequestParam(required=false) String note){

        return service.deposit(id, amount, note);

    }

    @PostMapping("/{id}/withdraw")

    public Mono<Account> withdraw(@PathVariable String id, @RequestParam BigDecimal amount,

                                  @RequestParam(required=false) String note){

        return service.withdraw(id, amount, note);

    }

    @GetMapping("/{id}/balance")

    public Mono<BigDecimal> balance(@PathVariable String id){ return service.getBalance(id); }

    @GetMapping("/{id}/movements")

    public Flux<AccountMovement> movements(@PathVariable String id){ return service.getMovements(id); }

    // Transfers
    @PostMapping("/transfer/internal/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> transferInternal(@PathVariable String customerId, @RequestBody TransferRequest req){
        return service.transferInternal(customerId, req.getFromAccountId(), req.getToAccountId(), req.getAmount(), req.getNote());
    }

    @PostMapping("/transfer/external")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> transferExternal(@RequestBody TransferRequest req,
                                      @RequestParam(required=false, defaultValue = "1.50") BigDecimal fee){
        return service.transferExternal(req.getFromAccountId(), req.getToAccountId(), req.getAmount(), req.getNote(), fee);
    }

    // Reports
    @GetMapping("/reports/average/{customerId}")
    public Flux<AverageBalanceResponse> averageBalances(@PathVariable String customerId,
                                                                               @RequestParam int year,
                                                                               @RequestParam int month){
        return service.averageBalancesForCustomerMonth(customerId, java.time.YearMonth.of(year, month));
    }

    @GetMapping("/reports/fees/{accountId}")
    public Mono<FeesReportResponse> fees(@PathVariable String accountId,
                                                                @RequestParam String from, @RequestParam String to){
        java.time.LocalDateTime f = java.time.LocalDateTime.parse(from);
        java.time.LocalDateTime t = java.time.LocalDateTime.parse(to);
        return service.feesReportForAccount(accountId, f, t);
    }
}

