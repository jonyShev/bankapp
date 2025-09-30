package com.jonyshev.accounts.controller;

import com.jonyshev.accounts.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal")
public class InternalBalanceController {

    private final BalanceService balanceService;

    @PostMapping("/add")
    public ResponseEntity<Void> add(@RequestParam String login, @RequestParam String currency, @RequestParam String amount) {
        balanceService.add(login, currency, new BigDecimal(amount));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sub")
    public ResponseEntity<Void> sub(@RequestParam String login, @RequestParam String currency, @RequestParam String amount) {
        boolean ok = balanceService.sub(login, currency, new BigDecimal(amount));
        return ok ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
