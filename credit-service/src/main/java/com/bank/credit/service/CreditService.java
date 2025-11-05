package com.bank.credit.service;

import com.bank.credit.domain.Credit;
import com.bank.credit.domain.CreditPayment;
import com.bank.credit.domain.CreditType;
import com.bank.credit.repository.CreditPaymentRepository;
import com.bank.credit.repository.CreditRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
@Service @RequiredArgsConstructor

public class CreditService {

    private final CreditRepository repo;

    private final CreditPaymentRepository payRepo;

    public Mono<Credit> create(Credit c){
        if (c.getType()==CreditType.PERSONAL){
            return repo.countByCustomerIdAndTypeAndActiveIsTrue(c.getCustomerId(), CreditType.PERSONAL)
                    .flatMap(count -> count>0 ? Mono.error(new IllegalStateException("Only one active personal credit per person"))
                            : repo.save(init(c)));

        }
        return repo.save(init(c)); // BUSINESS: multiple allowed
    }

    private Credit init(Credit c){
        c.setBalance(c.getBalance()==null? c.getPrincipal(): c.getBalance());
        c.setActive(true);
        c.setStartDate(c.getStartDate()==null? LocalDateTime.now(): c.getStartDate());
        return c;
    }

    public Flux<Credit> findAll(){ return repo.findAll(); }

    public Mono<Credit> update(String id, Credit c){
        return repo.findById(id).switchIfEmpty(Mono.error(new IllegalArgumentException("Not found")))
                .flatMap(db -> { c.setId(id);return repo.save(c); });

    }

    public Mono<Void> delete(String id){ return repo.deleteById(id); }

    // payments
    public Mono<Boolean> hasOverdueDebts(String customerId) { // CONSULTA OBLIGATORIA
        return repo.existsByCustomerIdAndOverdueIsTrue(customerId).defaultIfEmpty(false);
    }
    public Mono<Credit> pay(String creditId, BigDecimal amount, @Nullable String payerCustomerId){
        return repo.findById(creditId).flatMap(cr -> {
            if (!cr.isActive()) return Mono.error(new IllegalStateException("Credit not active"));
            if (amount.compareTo(BigDecimal.ZERO)<=0) return Mono.error(new IllegalArgumentException("Invalid amount"));
            BigDecimal newBal = cr.getBalance().subtract(amount);
            cr.setBalance(newBal.max(BigDecimal.ZERO));
            if (cr.getBalance().compareTo(BigDecimal.ZERO)==0) cr.setActive(false);
            //Si se pago, se poria desmrcar overdue
            if(cr.getBalance().compareTo(BigDecimal.ZERO)>0 && cr.getNextDueDate()!=null){
                cr.setOverdue(LocalDateTime.now().isAfter(cr.getNextDueDate())); //simple
            }else{
                cr.setOverdue(false);
            }
            CreditPayment p = CreditPayment.builder()
                    .creditId(cr.getId())
                    .timestamp(LocalDateTime.now())
                    .amount(amount)
                    .balanceAfter(cr.getBalance())
                    .payerCustomerId(payerCustomerId)
                    .build();
            return repo.save(cr).then(payRepo.save(p)).thenReturn(cr);
        });

    }
    public Flux<CreditPayment> last10Payments(String creditId){
        return payRepo.findByCreditIdOrderByTimestampDesc(creditId).take(10);
    }
    public Flux<CreditPayment> payments(String creditId){ return payRepo.findByCreditIdOrderByTimestampDesc(creditId); }

    public Mono<BigDecimal> balance(String creditId){ return repo.findById(creditId).map(Credit::getBalance); }

}

