package com.jonyshev.cash.client;

import com.jonyshev.commons.client.NotificationsClient;
import com.jonyshev.commons.model.EventType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AccountsClient {

    private final WebClient.Builder http;
    private final NotificationsClient notificationsClient;

    private WebClient client() {
        return http.baseUrl("http://accounts/api/internal").build();
    }

    public boolean add(String login, String currency, BigDecimal amount) {
        var response = postWithParams("/add", login, currency, amount);
        if (response) {
            notificationsClient.send(EventType.CASH_DEPOSIT, login, currency + " " + amount);
        }
        return response;
    }

    public boolean sub(String login, String currency, BigDecimal amount) {
        var response = postWithParams("/sub", login, currency, amount);
        if (response) {
            notificationsClient.send(EventType.CASH_WITHDRAW, login, currency + " " + amount);
        }
        return response;
    }

    @Retry(name = "s2s")
    @CircuitBreaker(name = "s2s")
    private boolean postWithParams(String path, String login, String currency, BigDecimal amount) {
        Mono<ResponseEntity<Void>> mono = client()
                .post()
                .uri((UriBuilder uri) -> uri.path(path)
                        .queryParam("login", login)
                        .queryParam("currency", currency)
                        .queryParam("amount", amount.toPlainString()) // безопасная конверсия
                        .build())
                .retrieve()
                .toBodilessEntity();

        return Boolean.TRUE.equals(mono
                .map(resp -> resp.getStatusCode().is2xxSuccessful())
                .onErrorReturn(false)
                .block());
    }
}
