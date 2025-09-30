package com.jonyshev.front.controller;

import com.jonyshev.commons.model.Currency;
import com.jonyshev.front.service.AccountsClient;
import com.jonyshev.front.service.CashClient;
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

    @PostMapping("/user/{login}/cash")
    public String cashOp(@PathVariable String login, @RequestParam String action, @RequestParam String currency,
                         @RequestParam String amount, Authentication auth, Model model) {
        if (auth == null || !auth.getName().equals(login)) {
            model.addAttribute("cashErrors", List.of("not_allowed"));
            return "main";
        }
        String err = switch (action) {
            case "deposit" -> cashClient.deposit(login, currency, amount);
            case "withdraw" -> cashClient.withdraw(login, currency, amount);
            default -> "unknown_action";
        };

        var userProfile = accountsClient.getUserProfile(login);
        var listUserProfile = accountsClient.getListUserProfile();
        model.addAttribute("login", userProfile.getLogin());
        model.addAttribute("name", userProfile.getName());
        model.addAttribute("birthdate", userProfile.getBirthdate());
        model.addAttribute("accounts", userProfile.getAccounts());
        model.addAttribute("currency", Currency.values());
        model.addAttribute("users", listUserProfile);

        model.addAttribute("cashErrors", err == null || err.isBlank() ? List.of() : List.of(err));
        model.addAttribute("transferErrors", List.of());
        model.addAttribute("exchangeErrors", List.of());

        return "main";
    }
}
