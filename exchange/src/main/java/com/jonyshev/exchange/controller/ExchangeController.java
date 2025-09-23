package com.jonyshev.exchange.controller;

import com.jonyshev.commons.dto.RateDto;
import com.jonyshev.commons.model.Currency;
import com.jonyshev.exchange.core.RateStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExchangeController {

    private final RateStorage storage;

    // Для фронта: GET /api/rates -> список RateDto
    @GetMapping("/api/rates")
    public List<RateDto> getRates() {
        return storage.toList();
    }

    // Для генератора: внутреннее обновление одного курса
    @PostMapping("/internal/rates/{code}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateRate(@PathVariable("code") String code, @RequestParam("value") double value) {
        storage.put(Currency.valueOf(code), value);
    }
}