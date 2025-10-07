package com.jonyshev.notifications.controller;

import com.jonyshev.commons.model.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notify")
public class NotifyController {
    private static final Logger log = LoggerFactory.getLogger(NotifyController.class);

    @PostMapping
    public ResponseEntity<Void> notify(@RequestBody NotificationRequest request) {
        log.info("NOTIFY: type={}, login={}, details={}", request.getEventType(), request.getLogin(), request.getDetails());
        return ResponseEntity.accepted().build();
    }
}
