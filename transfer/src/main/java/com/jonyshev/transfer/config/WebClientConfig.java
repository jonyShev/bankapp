package com.jonyshev.transfer.config;

import com.jonyshev.commons.client.NotificationsClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    @LoadBalanced
    WebClient.Builder http() {
        return WebClient.builder();
    }

    @Bean
    NotificationsClient notificationsClient(WebClient.Builder http) {
        return new NotificationsClient(http);
    }

}
