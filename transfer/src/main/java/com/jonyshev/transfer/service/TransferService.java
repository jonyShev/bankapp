package com.jonyshev.transfer.service;

import com.jonyshev.commons.client.NotificationsClient;
import com.jonyshev.commons.model.EventType;
import com.jonyshev.transfer.client.AccountsClient;
import com.jonyshev.transfer.client.BlockerClient;
import com.jonyshev.transfer.client.ExchangeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransferService {

    private static final RoundingMode RM = RoundingMode.HALF_UP;

    private final ExchangeClient exchangeClient;
    private final AccountsClient accountsClient;
    private final BlockerClient blockerClient;
    private final NotificationsClient notificationsClient;

    public record Result(boolean ok, String error) {
    }

    public Result transferSelf(String login, String fromCur, String toCur, BigDecimal amount) {
        var result = doTransfer(login, login, fromCur, toCur, amount);
        if (result.ok) {
            notificationsClient.send(EventType.TRANSFER_SELF, login, fromCur + " to " + toCur + " " + amount);
        }
        return result;
    }

    public Result transferToOther(String fromLogin, String toLogin, String fromCur, String toCur, BigDecimal amount) {
        var result = doTransfer(fromLogin, toLogin, fromCur, toCur, amount);
        if (result.ok) {
            notificationsClient.send(EventType.TRANSFER_TO, fromLogin + " to " + toLogin, fromCur + " to " + toCur + " " + amount);
        }
        return result;
    }

    private Result doTransfer(String fromLogin, String toLogin, String fromCur, String toCur, BigDecimal amount) {
        // антифрод по «списываемой» стороне
        var block = blockerClient.check(fromLogin, fromCur, amount);
        if (!block.isBlank()) return new Result(false, block);

        var rates = exchangeClient.rates();
        var amountTo = convert(amount, fromCur, toCur, rates);
        if (amountTo == null) return new Result(false, "rate_not_found");

        // списать/зачислить
        if (!accountsClient.sub(fromLogin, fromCur, amount)) return new Result(false, "insufficient_funds");
        if (!accountsClient.add(toLogin, toCur, amountTo)) return new Result(false, "cannot_credit");

        return new Result(true, "");
    }

    private BigDecimal convert(BigDecimal amount, String from, String to, Map<String, BigDecimal> rates) {
        if (from.equals(to)) return amount.setScale(4, RM);

        var rFrom = rates.get(from);
        var rTo = rates.get(to);
        if (rFrom == null || rTo == null) return null;

        var rub = amount.multiply(rFrom);
        return rub.divide(rTo, 4, RM);
    }
}




