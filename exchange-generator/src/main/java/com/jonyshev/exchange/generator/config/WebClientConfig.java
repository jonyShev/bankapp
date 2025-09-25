package com.jonyshev.exchange.generator.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    WebClient.Builder webClientBuilder(ServerOAuth2AuthorizedClientExchangeFilterFunction oauthFilter) {
        return WebClient.builder().filter(oauthFilter);
    }

    @Bean
    ServerOAuth2AuthorizedClientExchangeFilterFunction oauthFilter(
            ReactiveClientRegistrationRepository clients,
            ReactiveOAuth2AuthorizedClientService svc
    ) {
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
