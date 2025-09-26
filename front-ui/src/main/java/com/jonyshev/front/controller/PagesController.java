package com.jonyshev.front.controller;

import com.jonyshev.commons.model.Currency;
import com.jonyshev.front.service.AccountsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PagesController {

    private final AccountsClient accountsClient;

    @GetMapping("/")
    public String root() {
        return "redirect:/main";
    }

    @GetMapping("/main")
    public String main(Authentication auth, Model model) {
        String login = (auth != null ? auth.getName() : "guest");
        var userProfile = accountsClient.getUserProfile(login);

        model.addAttribute("login", userProfile.getLogin());
        model.addAttribute("name", userProfile.getName());
        model.addAttribute("birthdate", userProfile.getBirthdate());

        model.addAttribute("accounts", userProfile.getAccounts());

        model.addAttribute("users", List.of(userProfile));

        model.addAttribute("currency", Currency.values());

        model.addAttribute("cashErrors", List.of());
        model.addAttribute("transferErrors", List.of());
        model.addAttribute("exchangeErrors", List.of());

        return "main";
    }

    @GetMapping("/signup")
    public String signupGet(Model model) {
        model.addAttribute("errors", List.of());
        return "signup";
    }

    @PostMapping("/signup")
    public String signupPost(@RequestParam String login, @RequestParam String name, @RequestParam String password,
                             @RequestParam String birthdate, Model model) {
        if (login.isBlank() || password.isBlank() || name.isBlank() || birthdate.isBlank()) {
            model.addAttribute("errors", List.of("Все поля обязательны"));
            return "signup";
        }
        boolean ok = accountsClient.createUser(login, name, password, birthdate);
        if (!ok) {
            model.addAttribute("errors", List.of("Не удалось создать пользователя (возможно, логин занят)"));
            return "signup";
        }
        return "redirect:/main?login=" + login;
    }
}

