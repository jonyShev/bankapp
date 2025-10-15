package com.jonyshev.cash.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    @LoadBalanced
    WebClient.Builder webClientBuilder(ServletOAuth2AuthorizedClientExchangeFilterFunction oauth) {
        return WebClient.builder().filter(oauth);
    }

    @Bean
    ServletOAuth2AuthorizedClientExchangeFilterFunction oauth(
            ClientRegistrationRepository clients,
            OAuth2AuthorizedClientService svc
    ) {
        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clients, svc);
        var provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();
        manager.setAuthorizedClientProvider(provider);

        var oauth = new ServletOAuth2AuthorizedClientExchangeFilterFunction(manager);
        oauth.setDefaultClientRegistrationId("cash");
        return oauth;
    }

}
