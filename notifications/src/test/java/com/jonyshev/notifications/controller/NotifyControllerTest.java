package com.jonyshev.notifications.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonyshev.commons.model.EventType;
import com.jonyshev.commons.model.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotifyController.class)
class NotifyControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void notify_validBody_returns202() throws Exception {
        var req = new NotificationRequest(
                EventType.TRANSFER_TO,
                "alice",
                "EUR to USD 100"
        );

        mockMvc.perform(post("/api/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isAccepted());
    }

    @Test
    void notify_emptyJson_stillReturns202_becauseControllerDoesNotValidate() throws Exception {
        // Контроллер не валидирует поля -> {} распарсится с null'ами и вернёт 202
        mockMvc.perform(post("/api/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isAccepted());
    }
}
