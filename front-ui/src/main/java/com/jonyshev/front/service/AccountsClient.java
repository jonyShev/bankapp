package com.jonyshev.front.service;

import com.jonyshev.commons.dto.UserCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AccountsClient {

    private final WebClient.Builder http;

    public boolean createUser(String login, String name, String password, String birthdate) {
        var request = UserCreateRequest.builder()
                .login(login)
                .name(name)
                .password(password)
                .birthdate(birthdate)
                .build();

        return http.build()
                .post()
                .uri("http://accounts/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request) // объект → JSON
                .exchangeToMono(r -> Mono.just(r.statusCode().is2xxSuccessful()))
                .blockOptional()
                .orElse(false);
    }
}
