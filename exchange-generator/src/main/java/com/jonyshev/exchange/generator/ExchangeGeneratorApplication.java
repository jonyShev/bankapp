package com.jonyshev.exchange.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableScheduling
public class ExchangeGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExchangeGeneratorApplication.class, args);
    }

    @Bean
    @LoadBalanced
    WebClient.Builder webClientBuilder(ServerOAuth2AuthorizedClientExchangeFilterFunction oauthFilter) {
        return WebClient.builder().filter(oauthFilter);
    }

    @Bean
    ServerOAuth2AuthorizedClientExchangeFilterFunction oauthFilter(ReactiveClientRegistrationRepository clients,
                                                                   ReactiveOAuth2AuthorizedClientService svc) {
        var manager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clients, svc);
        var provider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();
        manager.setAuthorizedClientProvider(provider);

        var oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(manager);
        oauth.setDefaultClientRegistrationId("exchange-generator");
        return oauth;
    }
}
