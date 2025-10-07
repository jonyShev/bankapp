package com.jonyshev.commons.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    private EventType eventType;
    private String login;
    private String details;
}
