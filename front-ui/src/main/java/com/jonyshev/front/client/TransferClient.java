package com.jonyshev.front.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TransferClient {

    private final WebClient.Builder http;

    public TransferClient(@Qualifier("plainWebClientBuilder") WebClient.Builder http) {
        this.http = http;
    }

    private WebClient client() {
        return http.baseUrl("http://transfer/api/transfer").build();
    }

    @Retry(name="s2s")
    @CircuitBreaker(name="s2s")
    public String transferSelf(String login, String from, String to, String amount) {
        return client().post()
                .uri(uri -> uri.path("/self")
                        .queryParam("login", login)
                        .queryParam("fromCurrency", from)
                        .queryParam("toCurrency", to)
                        .queryParam("amount", amount)
                        .build())
                .retrieve()
                .toEntity(String.class)
                .map(resp -> resp.getStatusCode().is2xxSuccessful() ? "" : resp.getBody())
                .onErrorReturn("transfer_error")
                .block();
    }

    @Retry(name="s2s")
    @CircuitBreaker(name="s2s")
    public String transferToOther(String fromLogin, String toLogin, String from, String to, String amount) {
        return client().post()
                .uri(uri -> uri.path("/to-other")
                        .queryParam("fromLogin", fromLogin)
                        .queryParam("toLogin", toLogin)
                        .queryParam("fromCurrency", from)
                        .queryParam("toCurrency", to)
                        .queryParam("amount", amount)
                        .build())
                .retrieve()
                .toEntity(String.class)
                .map(resp -> resp.getStatusCode().is2xxSuccessful() ? "" : resp.getBody())
                .onErrorReturn("transfer_error")
                .block();
    }
}


