package com.jonyshev.front.config;

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

    @Bean("securedWebClientBuilder")
    @LoadBalanced
    WebClient.Builder securedWebClientBuilder(ServletOAuth2AuthorizedClientExchangeFilterFunction oauth) {
        return WebClient.builder().filter(oauth);
    }

    @Bean("plainWebClientBuilder")
    @LoadBalanced
    WebClient.Builder plainWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    ServletOAuth2AuthorizedClientExchangeFilterFunction oauth(
            ClientRegistrationRepository clients,
            OAuth2AuthorizedClientService svc
    ) {
        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clients, svc);
        manager.setAuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
        );
        var oauth = new ServletOAuth2AuthorizedClientExchangeFilterFunction(manager);
        oauth.setDefaultClientRegistrationId("front-ui");
        return oauth;
    }
}
