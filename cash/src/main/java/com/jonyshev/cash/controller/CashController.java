package com.jonyshev.cash.controller;

import com.jonyshev.cash.service.AccountsClient;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/cash")
public class CashController {

    private final AccountsClient accounts;

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestParam @NotBlank String login,
                                     @RequestParam @NotBlank String currency,
                                     @RequestParam BigDecimal amount) {
        return process(login, currency, amount, true);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestParam @NotBlank String login,
                                      @RequestParam @NotBlank String currency,
                                      @RequestParam BigDecimal amount) {
        return process(login, currency, amount, false);
    }

    private ResponseEntity<?> process(String login, String currency, BigDecimal amount, boolean isDeposit) {
        boolean ok = isDeposit
                ? accounts.add(login, currency, amount)
                : accounts.sub(login, currency, amount);

        if (!ok) {
            return ResponseEntity.badRequest()
                    .body(isDeposit ? "cannot_deposit" : "insufficient_funds");
        }
        return ResponseEntity.ok().build();
    }
}


