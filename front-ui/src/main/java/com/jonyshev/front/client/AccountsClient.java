package com.jonyshev.front.client;

import com.jonyshev.commons.dto.UserCreateRequest;
import com.jonyshev.commons.dto.UserProfileDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AccountsClient {

    private final WebClient.Builder http;

    public AccountsClient(@Qualifier("securedWebClientBuilder") WebClient.Builder http) {
        this.http = http;
    }

    @Retry(name = "s2s")
    @CircuitBreaker(name = "s2s")
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

    @Retry(name = "s2s")
    @CircuitBreaker(name = "s2s")
    public UserProfileDto getUserProfile(String login) {
        return http.build()
                .get()
                .uri("http://accounts/api/users/{login}", login)
                .retrieve()
                .bodyToMono(UserProfileDto.class)
                .block();
    }

    @Retry(name = "s2s")
    @CircuitBreaker(name = "s2s")
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

    @Retry(name = "s2s")
    @CircuitBreaker(name = "s2s")
    public List<UserProfileDto> getListUserProfile() {
        return http.build()
                .get()
                .uri("http://accounts/api/users")
                .retrieve()
                .bodyToFlux(UserProfileDto.class)
                .collectList()
                .block();
    }

    @Retry(name = "s2s")
    @CircuitBreaker(name = "s2s")
    public boolean changePassword(String login, String password, String confirm) {
        var body = """
                {"login":"%s","password":"%s","confirm":"%s"}
                """.formatted(login, password, confirm);
        return Boolean.TRUE.equals(http.build()
                .post().uri("http://accounts/api/internal/password")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchangeToMono(r -> reactor.core.publisher.Mono.just(r.statusCode().is2xxSuccessful()))
                .block());
    }

    @Retry(name = "s2s")
    @CircuitBreaker(name = "s2s")
    public String updateProfile(String login, String name, String birthdate) {
        var body = """
                {"login":"%s","name":"%s","birthdate":"%s"}
                """.formatted(login, name, birthdate);
        return http.build()
                .post().uri("http://accounts/api/internal/profile")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchangeToMono(r -> r.statusCode().is2xxSuccessful()
                        ? reactor.core.publisher.Mono.just("")
                        : r.bodyToMono(String.class))
                .block();
    }

    @Retry(name = "s2s")
    @CircuitBreaker(name = "s2s")
    public String updateAccounts(String login, java.util.List<String> currencies) {
        var jsonArray = (currencies == null || currencies.isEmpty())
                ? "[]"
                : currencies.stream().map(s -> "\"" + s + "\"").collect(java.util.stream.Collectors.joining(",", "[", "]"));
        var body = """
                {"login":"%s","accounts":%s}
                """.formatted(login, jsonArray);
        return http.build()
                .post().uri("http://accounts/api/internal/accounts")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchangeToMono(r -> r.statusCode().is2xxSuccessful()
                        ? reactor.core.publisher.Mono.just("")
                        : r.bodyToMono(String.class))
                .block();
    }
}
