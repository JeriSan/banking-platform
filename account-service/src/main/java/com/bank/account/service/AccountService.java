package com.bank.account.service;

import com.bank.account.domain.*;


import com.bank.account.repository.AccountMovementRepository;
import com.bank.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Service @RequiredArgsConstructor

public class AccountService {

    private final AccountRepository repo;

    private final AccountMovementRepository movRepo;

    public Mono<Account> create(Account a){

        // Validate type rules (personal vs business)

        if (Boolean.TRUE.equals(a.getBusiness())) {

            if (a.getType()==AccountType.SAVINGS || a.getType()==AccountType.FIXED_TERM)

                return Mono.error(new IllegalArgumentException("Business cannot have savings or fixed-term"));

            if (a.getOwners()==null || a.getOwners().isEmpty())

                return Mono.error(new IllegalArgumentException("Business account requires at least one owner"));

        } else {

            // personal limits

            Mono<Void> limitCheck = switch (a.getType()){

                case SAVINGS   -> repo.countByCustomerIdAndType(a.getCustomerId(), AccountType.SAVINGS)

                        .flatMap(c -> c>0 ? Mono.error(new IllegalStateException("Only one savings per personal")):Mono.empty());

                case CHECKING  -> repo.countByCustomerIdAndType(a.getCustomerId(), AccountType.CHECKING)

                        .flatMap(c -> c>0 ? Mono.error(new IllegalStateException("Only one checking per personal")):Mono.empty());

                case FIXED_TERM -> Mono.empty(); // multiple allowed

            };

            return limitCheck.then(repo.save(initDefaults(a)));

        }

        return repo.save(initDefaults(a));

    }

    private Account initDefaults(Account a){

        a.setBalance(a.getBalance()==null? BigDecimal.ZERO : a.getBalance());

        if (a.getType()==AccountType.SAVINGS){ a.setMaintenanceFee(false); a.setMonthlyMovementLimit(a.getMonthlyMovementLimit()==null? 20 : a.getMonthlyMovementLimit()); }

        if (a.getType()==AccountType.CHECKING){ a.setMaintenanceFee(true); a.setMonthlyMovementLimit(null); }

        if (a.getType()==AccountType.FIXED_TERM){ a.setMaintenanceFee(false); }

        return a;

    }

    public Flux<Account> findAll(){ return repo.findAll(); }

    public Mono<Account> update(String id, Account a){

        return repo.findById(id).switchIfEmpty(Mono.error(new IllegalArgumentException("Not found")))

                .flatMap(db -> { a.setId(id); return repo.save(a); });

    }

    public Mono<Void> delete(String id){ return repo.deleteById(id); }

    // ----- Operations: deposit/withdraw, balance, movements -----

    public Mono<Account> deposit(String accountId, BigDecimal amount, String note){

        return repo.findById(accountId).flatMap(acc -> {

            if (acc.getType()==AccountType.FIXED_TERM){

                int today = LocalDate.now().getDayOfMonth();

                if (acc.getFixedTermDayOfMonth()==null || acc.getFixedTermDayOfMonth()!=today)

                    return Mono.error(new IllegalStateException("Fixed-term movements allowed only on configured day"));

            }

            if (acc.getType()==AccountType.SAVINGS && acc.getMonthlyMovementLimit()!=null &&

                    acc.getMovementCountThisMonth() >= acc.getMonthlyMovementLimit())

                return Mono.error(new IllegalStateException("Monthly movement limit reached"));

            acc.setBalance(acc.getBalance().add(amount));

            acc.setMovementCountThisMonth(acc.getMovementCountThisMonth()+1);

            AccountMovement m = AccountMovement.builder()

                    .accountId(acc.getId()).timestamp(LocalDateTime.now()).type("DEPOSIT")

                    .amount(amount).balanceAfter(acc.getBalance()).note(note).build();

            return repo.save(acc).then(movRepo.save(m)).thenReturn(acc);

        });

    }

    public Mono<Account> withdraw(String accountId, BigDecimal amount, String note){

        return repo.findById(accountId).flatMap(acc -> {

            if (acc.getType()==AccountType.FIXED_TERM){

                int today = LocalDate.now().getDayOfMonth();

                if (acc.getFixedTermDayOfMonth()==null || acc.getFixedTermDayOfMonth()!=today)

                    return Mono.error(new IllegalStateException("Fixed-term movements allowed only on configured day"));

            }

            if (acc.getBalance().compareTo(amount) < 0)

                return Mono.error(new IllegalStateException("Insufficient balance"));

            if (acc.getType()==AccountType.SAVINGS && acc.getMonthlyMovementLimit()!=null &&

                    acc.getMovementCountThisMonth() >= acc.getMonthlyMovementLimit())

                return Mono.error(new IllegalStateException("Monthly movement limit reached"));

            acc.setBalance(acc.getBalance().subtract(amount));

            acc.setMovementCountThisMonth(acc.getMovementCountThisMonth()+1);

            AccountMovement m = AccountMovement.builder()

                    .accountId(acc.getId()).timestamp(LocalDateTime.now()).type("WITHDRAW")

                    .amount(amount).balanceAfter(acc.getBalance()).note(note).build();

            return repo.save(acc).then(movRepo.save(m)).thenReturn(acc);

        });

    }

    public Mono<BigDecimal> getBalance(String accountId){

        return repo.findById(accountId).map(Account::getBalance);

    }

    public Flux<AccountMovement> getMovements(String accountId){

        return movRepo.findByAccountIdOrderByTimestampDesc(accountId);

    }

}

