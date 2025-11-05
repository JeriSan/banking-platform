package com.bank.account.service;

import com.bank.account.domain.*;


import com.bank.account.dto.AverageBalanceResponse;
import com.bank.account.dto.FeesReportResponse;
import com.bank.account.repository.AccountMovementRepository;
import com.bank.account.repository.AccountRepository;
import com.bank.account.repository.DailyBalanceRepository;
import com.bank.account.repository.FeeMovementRepository;
import com.bank.account.webClient.CardClient;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;

import static com.bank.account.domain.AccountType.*;

@Service @RequiredArgsConstructor

public class AccountService {

    private final AccountRepository repo;
    private final AccountMovementRepository movRepo;
    private final FeeMovementRepository feeRepo;
    private final DailyBalanceRepository dailyRepo;
    private final CardClient cardClient;

    public Mono<Account> create(Account a){
        if(a.getMinimumOpeningAmount()!=null
        && a.getBalance()!=null
        && a.getBalance().compareTo(a.getMinimumOpeningAmount())<0){
            return Mono.error(new IllegalStateException("Opening balance too low"));
        }

        // maintenance fee off para CHECKING_PYME
        if (a.getType()==AccountType.CHECKING_PYME) {
            a.setMaintenanceFee(false);
            a.setRequiresCreditCard(true);
        }

        // VIP savings y PYME checking requieren tarjeta activa
        boolean requiresCard = Boolean.TRUE.equals(a.getRequiresCreditCard());
        Mono<Boolean> cardCheck = requiresCard
                ? cardClient.hasActiveCard(a.getCustomerId())
                : Mono.just(true);

        // defaults
        if (a.getBalance()==null) a.setBalance(java.math.BigDecimal.ZERO);
        if (a.getFreeTransactionsPerMonth()==null) a.setFreeTransactionsPerMonth(10);
        if (a.getFeePerExtraTransaction()==null) a.setFeePerExtraTransaction(new java.math.BigDecimal("1.00"));

        return cardCheck.flatMap(has -> {
            if (!has) return Mono.error(new IllegalStateException("Customer must have an active credit card"));

            //return repo.save(a);
            // Validate type rules (personal vs business)
            if (Boolean.TRUE.equals(a.getBusiness())) {

                if (a.getType()== SAVINGS || a.getType()== FIXED_TERM)
                    return Mono.error(new IllegalArgumentException("Business cannot have savings or fixed-term"));
                if (a.getOwners()==null || a.getOwners().isEmpty())
                    return Mono.error(new IllegalArgumentException("Business account requires at least one owner"));
            } else {
                // personal limits
                Mono<Void> limitCheck = switch (a.getType()){
                    case SAVINGS -> repo.countByCustomerIdAndType(a.getCustomerId(), SAVINGS)
                            .flatMap(c -> c>0 ? Mono.error(new IllegalStateException("Only one savings per personal")):Mono.empty());
                    case CHECKING -> repo.countByCustomerIdAndType(a.getCustomerId(), CHECKING)
                            .flatMap(c -> c>0 ? Mono.error(new IllegalStateException("Only one checking per personal")):Mono.empty());
                    case FIXED_TERM -> Mono.empty(); // multiple allowed
                    default -> Mono.empty();
                };
                return limitCheck.then(repo.save(initDefaults(a)));
            }
            return repo.save(initDefaults(a));
        });
    }

    private Account initDefaults(Account a){
        a.setBalance(a.getBalance()==null? BigDecimal.ZERO : a.getBalance());
        if (a.getType()== SAVINGS){ a.setMaintenanceFee(false); a.setMonthlyMovementLimit(a.getMonthlyMovementLimit()==null? 20 : a.getMonthlyMovementLimit()); }
        if (a.getType()== CHECKING){ a.setMaintenanceFee(true); a.setMonthlyMovementLimit(null); }
        if (a.getType()== FIXED_TERM){ a.setMaintenanceFee(false); }
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
            if (acc.getType()== FIXED_TERM){
                int today = LocalDate.now().getDayOfMonth();
                if (acc.getFixedTermDayOfMonth()==null || acc.getFixedTermDayOfMonth()!=today)
                    return Mono.error(new IllegalStateException("Fixed-term movements allowed only on configured day"));
            }
            if (acc.getType()== SAVINGS && acc.getMonthlyMovementLimit()!=null &&
                    acc.getMovementCountThisMonth() >= acc.getMonthlyMovementLimit())
                return Mono.error(new IllegalStateException("Monthly movement limit reached"));
            acc.setBalance(acc.getBalance().add(amount));
            Mono<Account> afterFee = applyExtraTxnFeeIfNeeded(acc);

            AccountMovement m = AccountMovement.builder()
                    .accountId(acc.getId())
                    .timestamp(LocalDateTime.now())
                    .type("DEPOSIT")
                    .amount(amount)
                    .balanceAfter(acc.getBalance())
                    .note(note)
                    .build();
            //return repo.save(acc).then(movRepo.save(m)).thenReturn(acc);
            return repo.save(acc).then(movRepo.save(m)).then(afterFee);

        });
    }

    private Mono<Account> applyExtraTxnFeeIfNeeded(Account acc){
        // incremento conteo y evaluar comisión
        acc.setMovementCountThisMonth(acc.getMovementCountThisMonth()+1);
        boolean chargeFee = acc.getFreeTransactionsPerMonth()!=null
                && acc.getMovementCountThisMonth() > acc.getFreeTransactionsPerMonth();
        if (!chargeFee) {
            return Mono.just(acc);
        }
        java.math.BigDecimal fee = acc.getFeePerExtraTransaction()!=null
                ? acc.getFeePerExtraTransaction()
                : BigDecimal.ZERO;
        if (fee.compareTo(BigDecimal.ZERO) > 0) {
            acc.setBalance(acc.getBalance().subtract(fee));
            FeeMovement feeMov = FeeMovement.builder()
                    .accountId(acc.getId())
                    .timestamp(LocalDateTime.now())
                    .amount(fee)
                    .reason("EXCESS_TRANSACTION")
                    .build();
            AccountMovement mFee = AccountMovement.builder()
                    .accountId(acc.getId())
                    .timestamp(LocalDateTime.now())
                    .type("FEE")
                    .amount(fee.negate())
                    .balanceAfter(acc.getBalance())
                    .note("Excess transaction fee")
                    .build();
            return feeRepo.save(feeMov)
                    .then(movRepo.save(mFee))
                    .then(repo.save(acc));
        }
        return Mono.just(acc);
    }

    public Mono<Account> withdraw(String accountId, BigDecimal amount, String note){
        return repo.findById(accountId).flatMap(acc -> {
            if (acc.getType()== FIXED_TERM){
                int today = LocalDate.now().getDayOfMonth();
                if (acc.getFixedTermDayOfMonth()==null || acc.getFixedTermDayOfMonth()!=today)
                    return Mono.error(new IllegalStateException("Fixed-term movements allowed only on configured day"));
            }
            if (acc.getBalance().compareTo(amount) < 0)
                return Mono.error(new IllegalStateException("Insufficient balance"));
            if (acc.getType()== SAVINGS && acc.getMonthlyMovementLimit()!=null &&
                    acc.getMovementCountThisMonth() >= acc.getMonthlyMovementLimit())
                return Mono.error(new IllegalStateException("Monthly movement limit reached"));

            acc.setBalance(acc.getBalance().subtract(amount));
            Mono<Account> afterFee = applyExtraTxnFeeIfNeeded(acc);
            acc.setMovementCountThisMonth(acc.getMovementCountThisMonth()+1);
            AccountMovement m = AccountMovement.builder()
                    .accountId(acc.getId())
                    .timestamp(LocalDateTime.now())
                    .type("WITHDRAW")
                    .amount(amount)
                    .balanceAfter(acc.getBalance())
                    .note(note)
                    .build();
           // return repo.save(acc).then(movRepo.save(m)).thenReturn(acc);
            return repo.save(acc).then(movRepo.save(m)).then(afterFee);

        });
    }

    public Mono<BigDecimal> getBalance(String accountId){
        return repo.findById(accountId).map(Account::getBalance);
    }

    public Flux<AccountMovement> getMovements(String accountId){
        return movRepo.findByAccountIdOrderByTimestampDesc(accountId);
    }

    // ---- Transfers ----
    public Mono<Void> transferInternal(String customerId, String fromId, String toId, BigDecimal amount, String note){
        if (fromId.equals(toId)) return Mono.error(new IllegalArgumentException("Same account"));
        return repo.findById(fromId).zipWith(repo.findById(toId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found")))
                .flatMap(tuple -> {
                    Account from = tuple.getT1(); Account to = tuple.getT2();
                    if (!from.getCustomerId().equals(customerId) || !to.getCustomerId().equals(customerId))
                        return Mono.error(new IllegalStateException("Not same owner"));

                    return withdraw(from.getId(), amount, "Transfer to " + toId + (note!=null? " - "+note:""))
                            .flatMap(updatedFrom -> {
                                // registrar movimiento específico de transferencia (salida)
                                AccountMovement out = AccountMovement.builder()
                                        .accountId(from.getId())
                                        .timestamp(java.time.LocalDateTime.now())
                                        .type("TRANSFER_OUT")
                                        .amount(amount.negate())
                                        .balanceAfter(updatedFrom.getBalance())
                                        .note("Internal transfer to "+toId)
                                        .build();
                                return movRepo.save(out).thenReturn(updatedFrom);
                            })
                            .flatMap(ignore -> deposit(to.getId(), amount, "Transfer from " + fromId + (note!=null? " - "+note:"")))
                            .flatMap(updatedTo -> {
                                AccountMovement in = AccountMovement.builder()
                                        .accountId(to.getId())
                                        .timestamp(java.time.LocalDateTime.now())
                                        .type("TRANSFER_IN")
                                        .amount(amount)
                                        .balanceAfter(updatedTo.getBalance())
                                        .note("Internal transfer from "+fromId)
                                        .build();
                                return movRepo.save(in).then();
                            });
                });
    }
    public Mono<Void> transferExternal(String fromId, String toId, BigDecimal amount, String note, BigDecimal externalTransferFee){
        if (fromId.equals(toId)) return Mono.error(new IllegalArgumentException("Same account")); //funciona
        return repo.findById(fromId).zipWith(repo.findById(toId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found")))
                .flatMap(tuple -> {
                    Account from = tuple.getT1(); Account to = tuple.getT2();
                    // diferentes dueños permitidos
                    return withdraw(from.getId(), amount, "Transfer to " + toId + (note!=null? " - "+note:""))
                            .flatMap(updatedFrom -> {
                                AccountMovement out = AccountMovement.builder()
                                        .accountId(from.getId())
                                        .timestamp(java.time.LocalDateTime.now())
                                        .type("TRANSFER_OUT")
                                        .amount(amount.negate())
                                        .balanceAfter(updatedFrom.getBalance())
                                        .note("External transfer to "+toId)
                                        .build();
                                return movRepo.save(out).thenReturn(updatedFrom);
                            })
                            .flatMap(ignore -> deposit(to.getId(), amount, "Transfer from " + fromId + (note!=null? " - "+note:"")))
                            .flatMap(updatedTo -> {
                                AccountMovement in = AccountMovement.builder()
                                        .accountId(to.getId())
                                        .timestamp(java.time.LocalDateTime.now())
                                        .type("TRANSFER_IN")
                                        .amount(amount)
                                        .balanceAfter(updatedTo.getBalance())
                                        .note("External transfer from "+fromId)
                                        .build();

                                // cobrar comisión de transferencia al FROM
                                return movRepo.save(in)
                                        .then(chargeTransferFee(fromId, externalTransferFee))
                                        .then();
                            });
                });
    }

    private Mono<Void> chargeTransferFee(String accountId, java.math.BigDecimal fee){
        if (fee==null || fee.compareTo(java.math.BigDecimal.ZERO)<=0) return Mono.empty();
        return repo.findById(accountId).flatMap(acc -> {
            if (acc.getBalance().compareTo(fee) < 0) return Mono.error(new IllegalStateException("Insufficient balance for transfer fee"));
            acc.setBalance(acc.getBalance().subtract(fee));

            FeeMovement feeMov = FeeMovement.builder()
                    .accountId(acc.getId())
                    .timestamp(java.time.LocalDateTime.now())
                    .amount(fee)
                    .reason("TRANSFER_FEE")
                    .build();
            AccountMovement mFee = AccountMovement.builder()
                    .accountId(acc.getId())
                    .timestamp(java.time.LocalDateTime.now())
                    .type("FEE")
                    .amount(fee.negate())
                    .balanceAfter(acc.getBalance())
                    .note("External transfer fee")
                    .build();

            return feeRepo.save(feeMov).then(movRepo.save(mFee)).then(repo.save(acc)).then();
        });
    }

    // ---- Reports ----
    public Flux<AverageBalanceResponse> averageBalancesForCustomerMonth(String customerId, YearMonth ym){
        LocalDateTime startDate = ym.atDay(1).atStartOfDay();
        LocalDateTime endDate = ym.atEndOfMonth().atTime(23, 59,59);
        return repo.findByCustomerId(customerId)
                .flatMap(ac -> dailyRepo.findByProductIdAndDateBetween(ac.getId(), startDate, endDate)
                        .collectList()
                        .map(list -> {
                            java.math.BigDecimal sum = list.stream()
                                    .map(DailyBalance::getBalance)
                                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                            java.math.BigDecimal avg = list.isEmpty()
                                    ? java.math.BigDecimal.ZERO
                                    : sum.divide(java.math.BigDecimal.valueOf(ym.lengthOfMonth()), java.math.RoundingMode.HALF_UP);
                            return AverageBalanceResponse.builder()
                                    .productId(ac.getId())
                                    .yearMonth(ym)
                                    .averageDailyBalance(avg)
                                    .build();
                        })
                );
    }

    public Mono<FeesReportResponse> feesReportForAccount(String accountId, java.time.LocalDateTime from, java.time.LocalDateTime to){
        return feeRepo.findByAccountIdAndTimestampBetween(accountId, from, to).collectList()
                .map(list -> {
                    java.math.BigDecimal total = list.stream()
                            .map(FeeMovement::getAmount)
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                    return FeesReportResponse.builder()
                            .accountId(accountId)
                            .from(from).to(to)
                            .totalFees(total)
                            .details(list)
                            .build();
                });
    }

}

