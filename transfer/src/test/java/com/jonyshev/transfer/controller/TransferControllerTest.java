package com.jonyshev.transfer.controller;

import com.jonyshev.transfer.service.TransferService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferService transferService;

    @Test
    void self_ok_returns200() throws Exception {
        // given
        Mockito.when(transferService.transferSelf(anyString(), anyString(), anyString(), any(BigDecimal.class)))
                .thenReturn(new TransferService.Result(true, ""));

        // when + then
        mockMvc.perform(post("/api/transfer/self")
                        .param("login", "alice")
                        .param("fromCurrency", "EUR")
                        .param("toCurrency", "USD")
                        .param("amount", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // тело пустое при ok
    }

    @Test
    void self_error_returns400_withMessage() throws Exception {
        Mockito.when(transferService.transferSelf(anyString(), anyString(), anyString(), any(BigDecimal.class)))
                .thenReturn(new TransferService.Result(false, "insufficient_funds"));

        mockMvc.perform(post("/api/transfer/self")
                        .param("login", "alice")
                        .param("fromCurrency", "EUR")
                        .param("toCurrency", "USD")
                        .param("amount", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("insufficient_funds"));
    }

    @Test
    void toOther_ok_returns200() throws Exception {
        Mockito.when(transferService.transferToOther(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class)))
                .thenReturn(new TransferService.Result(true, ""));

        mockMvc.perform(post("/api/transfer/to-other")
                        .param("fromLogin", "alice")
                        .param("toLogin", "bob")
                        .param("fromCurrency", "EUR")
                        .param("toCurrency", "USD")
                        .param("amount", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void toOther_error_returns400_withMessage() throws Exception {
        Mockito.when(transferService.transferToOther(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class)))
                .thenReturn(new TransferService.Result(false, "blocked_by_fraud"));

        mockMvc.perform(post("/api/transfer/to-other")
                        .param("fromLogin", "alice")
                        .param("toLogin", "bob")
                        .param("fromCurrency", "EUR")
                        .param("toCurrency", "USD")
                        .param("amount", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("blocked_by_fraud"));
    }
}
