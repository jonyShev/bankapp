package com.jonyshev.front.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class TransferClient {

    private final WebClient.Builder http;

    private WebClient client() {
        return http.baseUrl("http://transfer/api/transfer").build();
    }

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


