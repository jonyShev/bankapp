package com.jonyshev.front.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class CashClient {

    private final WebClient.Builder http;

    public String deposit(String login, String currency, String amount) {
        return callCash("/deposit", login, currency, amount);
    }

    public String withdraw(String login, String currency, String amount) {
        return callCash("/withdraw", login, currency, amount);
    }

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

