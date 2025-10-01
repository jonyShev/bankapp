package com.jonyshev.transfer.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class BlockerClient {

    private final WebClient.Builder http;

    private WebClient client() {
        return http.baseUrl("http://blocker/api").build();
    }

    /**
     * Возвращает "" если всё ок, иначе текстовый код ошибки (например, "blocked_limit").
     */
    public String check(String login, String currency, BigDecimal amount) {
        return client().post()
                .uri(u -> u.path("/block")
                        .queryParam("login", login)
                        .queryParam("currency", currency)
                        .queryParam("amount", amount)
                        .build())
                .retrieve()
                .toEntity(String.class)
                .map(resp -> resp.getStatusCode().is2xxSuccessful()
                        ? ""
                        : (resp.getBody() == null || resp.getBody().isBlank() ? "blocked" : resp.getBody()))
                // на сетевые/прочие ошибки вернём технический код
                .onErrorReturn("blocked_error")
                .block();
    }
}
