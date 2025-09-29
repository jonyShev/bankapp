package com.jonyshev.front.service;

import com.jonyshev.commons.dto.UserCreateRequest;
import com.jonyshev.commons.dto.UserProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

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

    public UserProfileDto getUserProfile(String login) {
        return http.build()
                .get()
                .uri("http://accounts/api/users/{login}", login)
                .retrieve()
                .bodyToMono(UserProfileDto.class)
                .block();
    }

    public boolean auth(String login, String password) {
        return Boolean.TRUE.equals(
                http.build()
                        .post()
                        .uri("http://accounts/api/internal/auth?login={login}&password={password}", login, password)
                        .retrieve()
                        .toBodilessEntity()
                        .map(resp -> resp.getStatusCode().is2xxSuccessful())
                        .onErrorReturn(false)
                        .block());
    }

    public List<UserProfileDto> getListUserProfile() {
        return http.build()
                .get()
                .uri("http://accounts/api/users")
                .retrieve()
                .bodyToFlux(UserProfileDto.class)
                .collectList()
                .block();
    }
}
