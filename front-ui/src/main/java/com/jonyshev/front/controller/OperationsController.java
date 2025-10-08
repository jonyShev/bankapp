package com.jonyshev.front.controller;

import com.jonyshev.commons.model.Currency;
import com.jonyshev.front.client.TransferClient;
import com.jonyshev.front.client.AccountsClient;
import com.jonyshev.front.client.CashClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OperationsController {

    private final CashClient cashClient;
    private final AccountsClient accountsClient;
    private final TransferClient transferClient;

    @PostMapping("/user/{login}/cash")
    public String cashOp(@PathVariable String login,
                         @RequestParam String action,
                         @RequestParam String currency,
                         @RequestParam String amount,
                         Authentication auth,
                         Model model) {
        if (!isOwn(auth, login)) {
            model.addAttribute("cashErrors", List.of("not_allowed"));
            model.addAttribute("transferErrors", List.of());
            model.addAttribute("exchangeErrors", List.of());
            fillMainModel(login, model);
            return "main";
        }

        String err = switch (action) {
            case "deposit" -> cashClient.deposit(login, currency, amount);
            case "withdraw" -> cashClient.withdraw(login, currency, amount);
            default -> "unknown_action";
        };

        fillMainModel(login, model);
        model.addAttribute("cashErrors", err == null || err.isBlank() ? List.of() : List.of(err));
        model.addAttribute("transferErrors", List.of());
        model.addAttribute("exchangeErrors", List.of());
        return "main";
    }

    @PostMapping("/user/{login}/transfer/self")
    public String transferSelf(@PathVariable String login,
                               @RequestParam String from,
                               @RequestParam String to,
                               @RequestParam String amount,
                               Authentication auth,
                               Model model) {
        if (!isOwn(auth, login)) {
            model.addAttribute("transferErrors", List.of("not_allowed"));
            model.addAttribute("cashErrors", List.of());
            model.addAttribute("exchangeErrors", List.of());
            fillMainModel(login, model);
            return "main";
        }

        String err = transferClient.transferSelf(login, from, to, amount);

        fillMainModel(login, model);
        model.addAttribute("transferErrors", err.isBlank() ? List.of() : List.of(err));
        model.addAttribute("cashErrors", List.of());
        model.addAttribute("exchangeErrors", List.of());
        return "main";
    }

    @PostMapping("/user/{login}/transfer/to")
    public String transferTo(@PathVariable String login,
                             @RequestParam String toLogin,
                             @RequestParam String from,
                             @RequestParam String to,
                             @RequestParam String amount,
                             Authentication auth,
                             Model model) {
        if (!isOwn(auth, login)) {
            model.addAttribute("transferErrors", List.of("not_allowed"));
            model.addAttribute("cashErrors", List.of());
            model.addAttribute("exchangeErrors", List.of());
            fillMainModel(login, model);
            return "main";
        }

        String err = transferClient.transferToOther(login, toLogin, from, to, amount);

        fillMainModel(login, model);
        model.addAttribute("transferErrors", err.isBlank() ? List.of() : List.of(err));
        model.addAttribute("cashErrors", List.of());
        model.addAttribute("exchangeErrors", List.of());
        return "main";
    }

    // --- helpers ---

    private boolean isOwn(Authentication auth, String login) {
        return auth != null && auth.getName().equals(login);
    }

    private void fillMainModel(String login, Model model) {
        var userProfile = accountsClient.getUserProfile(login);
        var listUserProfile = accountsClient.getListUserProfile();

        model.addAttribute("login", userProfile.getLogin());
        model.addAttribute("name", userProfile.getName());
        model.addAttribute("birthdate", userProfile.getBirthdate());
        model.addAttribute("accounts", userProfile.getAccounts());
        model.addAttribute("users", listUserProfile);
        model.addAttribute("currency", Currency.values());
    }
}
