package com.jonyshev.exchange.controller;

import com.jonyshev.commons.dto.RateDto;
import com.jonyshev.commons.model.Currency;
import com.jonyshev.exchange.service.RateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExchangeController {

    private final RateService rateService;


    // Для фронта: GET /api/rates -> список RateDto
    @GetMapping("/api/rates")
    public List<RateDto> getRates() {
        return rateService.getAll();
    }

    // Для генератора: внутреннее обновление одного курса
    @PostMapping("/internal/rates/{code}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateRate(@PathVariable("code") String code, @RequestParam("value") BigDecimal value) {
        rateService.upsert(Currency.valueOf(code), value);
    }
}