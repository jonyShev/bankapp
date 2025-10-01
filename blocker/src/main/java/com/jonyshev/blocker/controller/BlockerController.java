package com.jonyshev.blocker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/block")
public class BlockerController {
    @PostMapping
    public ResponseEntity<String> check(@RequestParam String login, @RequestParam String currency, @RequestParam String amount) {
        try {
            var sum = new BigDecimal(amount);
            if (sum.compareTo(new BigDecimal("1000000")) > 0) return ResponseEntity.badRequest().body("blocked_limit");
            if (ThreadLocalRandom.current().nextInt(100) < 3) return ResponseEntity.badRequest().body("blocked_random");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("invalid_amount");
        }
    }
}