package com.bank.summary.controller;

import com.bank.summary.dto.CustomerSummary;
import com.bank.summary.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/summary") @RequiredArgsConstructor
public class SummaryController {
    private final SummaryService service;

    @GetMapping("/{customerId}")
    public Mono<CustomerSummary> consolidated(@PathVariable String customerId){
        return service.consolidated(customerId);
    }

    @GetMapping("/reports/products")
    public Mono<Object> productReport(@RequestParam String type,
                                      @RequestParam String from,
                                      @RequestParam String to){
        return service.productReport(type, from, to);
    }
}