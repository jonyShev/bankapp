package com.jonyshev.transfer.controller;


import com.jonyshev.transfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transfer")
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/self")
    public ResponseEntity<String> self(@RequestParam String login,
                                       @RequestParam String fromCurrency,
                                       @RequestParam String toCurrency,
                                       @RequestParam BigDecimal amount) {
        var result = transferService.transferSelf(login, fromCurrency, toCurrency, amount);
        return result.ok() ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body(result.error());
    }

    @PostMapping("/to-other")
    public ResponseEntity<String> toOther(@RequestParam String fromLogin,
                                          @RequestParam String toLogin,
                                          @RequestParam String fromCurrency,
                                          @RequestParam String toCurrency,
                                          @RequestParam BigDecimal amount) {
        var result = transferService.transferToOther(fromLogin, toLogin, fromCurrency, toCurrency, amount);
        return result.ok() ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body(result.error());
    }
}

