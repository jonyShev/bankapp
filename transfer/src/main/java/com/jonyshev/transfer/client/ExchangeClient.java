package com.jonyshev.transfer.client;

import com.jonyshev.commons.dto.RateDto;
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
