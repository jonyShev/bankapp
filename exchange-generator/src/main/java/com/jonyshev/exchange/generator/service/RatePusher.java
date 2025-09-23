package com.jonyshev.exchange.generator.service;


import com.jonyshev.commons.model.Currency;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class RatePusher {

    private final WebClient.Builder http;

    // базовые значения
    private final Map<Currency, Double> base = Map.of(
            Currency.RUB, 1.0,
            Currency.USD, 90.0,
            Currency.CNY, 12.0
    );

    @Scheduled(fixedRate = 1000)
    @Retry(name = "push")
    public void tick() {
        for (var e : base.entrySet()) {
            var c = e.getKey();
            double v = jiggle(e.getValue());
            // идём прям в сервис exchange по имени (через Consul)
            http.build()
                    .post()
                    .uri("http://exchange/internal/rates/{code}?value={v}", c.name(), v)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        }
    }

    private double jiggle(double base) {
        double delta = (ThreadLocalRandom.current().nextDouble() - 0.5) / 100.0;
        double val = base * (1.0 + delta);
        return Math.round(val * 100.0) / 100.0;
    }
}