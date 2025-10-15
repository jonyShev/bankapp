package com.jonyshev.transfer.service;

import com.jonyshev.commons.model.EventType;
import com.jonyshev.transfer.client.AccountsClient;
import com.jonyshev.transfer.client.BlockerClient;
import com.jonyshev.transfer.client.ExchangeClient;
import com.jonyshev.transfer.client.NotificationsClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    ExchangeClient exchangeClient;
    @Mock
    AccountsClient accountsClient;
    @Mock
    BlockerClient blockerClient;
    @Mock
    NotificationsClient notificationsClient;

    TransferService service;

    @Captor
    ArgumentCaptor<BigDecimal> amountCaptor;

    // фиктивные курсы «в рублях»
    private final Map<String, BigDecimal> RATES = Map.of(
            "RUB", new BigDecimal("1"),
            "USD", new BigDecimal("90"),
            "EUR", new BigDecimal("100"),
            "CNY", new BigDecimal("12")
    );

    @BeforeEach
    void setUp() {
        service = new TransferService(exchangeClient, accountsClient, blockerClient, notificationsClient);
    }

    @Test
    void transferSelf_success_differentCurrencies_convertsAndNotifies() {
        when(blockerClient.check("alice", "USD", new BigDecimal("10.00"))).thenReturn("");
        when(exchangeClient.rates()).thenReturn(RATES);
        when(accountsClient.sub("alice", "USD", new BigDecimal("10.00"))).thenReturn(true);
        when(accountsClient.add(eq("alice"), eq("EUR"), any(BigDecimal.class))).thenReturn(true);

        var result = service.transferSelf("alice", "USD", "EUR", new BigDecimal("10.00"));

        assertThat(result.ok()).isTrue();
        assertThat(result.error()).isEmpty();

        // Проверяем, что списали 10.00 USD
        verify(accountsClient).sub("alice", "USD", new BigDecimal("10.00"));

        // Проверяем, что зачислили 9.0000 EUR (10 * 90 / 100)
        verify(accountsClient).add(eq("alice"), eq("EUR"), amountCaptor.capture());
        assertThat(amountCaptor.getValue()).isEqualByComparingTo(new BigDecimal("9.0000"));

        // Уведомление отправлено
        verify(notificationsClient).send(
                eq(EventType.TRANSFER_SELF),
                eq("alice"),
                eq("USD to EUR 10.00")
        );
    }

    @Test
    void transferToOther_success_notifiesWithBothLogins() {
        when(blockerClient.check("alice", "RUB", new BigDecimal("1500"))).thenReturn("");
        when(exchangeClient.rates()).thenReturn(RATES);
        when(accountsClient.sub("alice", "RUB", new BigDecimal("1500"))).thenReturn(true);
        when(accountsClient.add(eq("bob"), eq("USD"), any(BigDecimal.class))).thenReturn(true);

        var result = service.transferToOther("alice", "bob", "RUB", "USD", new BigDecimal("1500"));

        assertThat(result.ok()).isTrue();
        verify(accountsClient).add(eq("bob"), eq("USD"), amountCaptor.capture());
        // 1500 RUB -> USD: 1500 * 1 / 90 = 16.6667 (HALF_UP, scale 4)
        assertThat(amountCaptor.getValue()).isEqualByComparingTo(new BigDecimal("16.6667"));

        verify(notificationsClient).send(
                eq(EventType.TRANSFER_TO),
                eq("alice to bob"),
                eq("RUB to USD 1500")
        );
    }

    @Test
    void transferSelf_sameCurrency_scalesTo4_andNotifies() {
        when(blockerClient.check("alice", "EUR", new BigDecimal("1"))).thenReturn("");
        when(exchangeClient.rates()).thenReturn(Map.of()); // не нужен при одинаковых валютах
        when(accountsClient.sub("alice", "EUR", new BigDecimal("1"))).thenReturn(true);
        when(accountsClient.add("alice", "EUR", new BigDecimal("1.0000"))).thenReturn(true);

        var result = service.transferSelf("alice", "EUR", "EUR", new BigDecimal("1"));

        assertThat(result.ok()).isTrue();
        verify(accountsClient).add("alice", "EUR", new BigDecimal("1.0000"));
        verify(notificationsClient).send(
                eq(EventType.TRANSFER_SELF),
                eq("alice"),
                eq("EUR to EUR 1")
        );
    }

    @Test
    void transfer_fraudBlocked_returnsError_andStopsEarly() {
        when(blockerClient.check("alice", "USD", new BigDecimal("10"))).thenReturn("blocked_by_rules");

        var result = service.transferSelf("alice", "USD", "EUR", new BigDecimal("10"));

        assertThat(result.ok()).isFalse();
        assertThat(result.error()).isEqualTo("blocked_by_rules");

        verifyNoInteractions(exchangeClient);
        verifyNoInteractions(accountsClient);
        verifyNoInteractions(notificationsClient);
    }

    @Test
    void transfer_rateMissing_returnsRateNotFound() {
        when(blockerClient.check("alice", "USD", new BigDecimal("10"))).thenReturn("");
        // Пусть нет курса EUR
        when(exchangeClient.rates()).thenReturn(Map.of("USD", new BigDecimal("90")));

        var result = service.transferSelf("alice", "USD", "EUR", new BigDecimal("10"));

        assertThat(result.ok()).isFalse();
        assertThat(result.error()).isEqualTo("rate_not_found");

        verify(accountsClient, never()).sub(any(), any(), any());
        verifyNoInteractions(notificationsClient);
    }

    @Test
    void transfer_insufficientFunds_returnsError() {
        when(blockerClient.check("alice", "USD", new BigDecimal("10.00"))).thenReturn("");
        when(exchangeClient.rates()).thenReturn(RATES);
        when(accountsClient.sub("alice", "USD", new BigDecimal("10.00"))).thenReturn(false);

        var result = service.transferSelf("alice", "USD", "EUR", new BigDecimal("10.00"));

        assertThat(result.ok()).isFalse();
        assertThat(result.error()).isEqualTo("insufficient_funds");

        verify(accountsClient, never()).add(any(), any(), any());
        verifyNoInteractions(notificationsClient);
    }

    @Test
    void transfer_cannotCredit_returnsError() {
        when(blockerClient.check("alice", "USD", new BigDecimal("10.00"))).thenReturn("");
        when(exchangeClient.rates()).thenReturn(RATES);
        when(accountsClient.sub("alice", "USD", new BigDecimal("10.00"))).thenReturn(true);
        when(accountsClient.add(eq("alice"), eq("EUR"), any(BigDecimal.class))).thenReturn(false);

        var result = service.transferSelf("alice", "USD", "EUR", new BigDecimal("10.00"));

        assertThat(result.ok()).isFalse();
        assertThat(result.error()).isEqualTo("cannot_credit");

        verifyNoInteractions(notificationsClient);
    }
}
