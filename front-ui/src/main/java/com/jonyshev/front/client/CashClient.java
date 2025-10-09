package com.jonyshev.front.client;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class CashClient {

    private final WebClient.Builder http;

    public CashClient(@Qualifier("plainWebClientBuilder") WebClient.Builder http) {
        this.http = http;
    }

    public String deposit(String login, String currency, String amount) {
        return callCash("/deposit", login, currency, amount);
    }

    public String withdraw(String login, String currency, String amount) {
        return callCash("/withdraw", login, currency, amount);
    }

    @Retry(name="s2s")
    @CircuitBreaker(name="s2s")
    private String callCash(String action, String login, String currency, String amount) {
        return http.baseUrl("http://cash/api/cash").build()
                .post()
                .uri(uri -> uri.path(action)
                        .queryParam("login", login)
                        .queryParam("currency", currency)
                        .queryParam("amount", amount)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .onErrorReturn("service_cash_unavailable")
                .blockOptional()
                .orElse("");
    }
}

