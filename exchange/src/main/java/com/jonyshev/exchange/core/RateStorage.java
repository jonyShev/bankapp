package com.jonyshev.exchange.core;

import com.jonyshev.commons.dto.RateDto;
import com.jonyshev.commons.model.Currency;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateStorage {
    private final Map<Currency, Double> rates = new ConcurrentHashMap<>();

    public RateStorage() {
        // стартовые значения
        rates.put(Currency.RUB, 1.0);
        rates.put(Currency.USD, 90.0);
        rates.put(Currency.CNY, 12.0);
    }

    public List<RateDto> toList() {
        List<RateDto> list = new ArrayList<>();
        for (var e : rates.entrySet()) {
            list.add(new RateDto(e.getKey().name(), e.getKey().getTitle(), e.getValue()));
        }
        return list;
    }

    public void put(Currency currency, double value) {
        rates.put(currency, value);
    }
}