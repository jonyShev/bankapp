package com.jonyshev.transfer.client;

import com.jonyshev.commons.model.EventType;
import com.jonyshev.commons.model.NotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class NotificationsClient {

    private final WebClient.Builder http;

    public void send(EventType type, String login, String details) {
        var req = new NotificationRequest(type, login, details);
        http.build()
                .post()
                .uri("http://notifications/api/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
