package com.jonyshev.transfer.client;

import com.jonyshev.commons.dto.RateDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExchangeClient {

    private final WebClient.Builder http;

    @Retry(name = "s2s")
    @CircuitBreaker(name = "s2s")
    public Map<String, BigDecimal> rates() {
        var list = Arrays.asList(
                http.build().get()
                        .uri("http://exchange/api/rates")
                        .retrieve()
                        .bodyToMono(RateDto[].class)
                        .block()
        );
        return list.stream()
                .collect(Collectors.toMap(RateDto::getTitle, RateDto::getValue));
    }
}
