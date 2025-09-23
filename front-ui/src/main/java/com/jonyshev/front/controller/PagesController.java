package com.jonyshev.front.controller;

import com.jonyshev.commons.model.Currency;

import com.jonyshev.front.model.ViewModels.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class PagesController {

    @GetMapping("/")
    public String root() {
        return "redirect:/main";
    }

    @GetMapping("/main")
    public String main(Model model,
                       @RequestParam(value = "login", required = false, defaultValue = "vasya") String login) {
        model.addAttribute("login", login);
        model.addAttribute("name", "Вася Пупкин");
        model.addAttribute("birthdate", "1990-01-01");

        model.addAttribute("users", List.of(
                new UserVm("vasya", "Вася Пупкин", "1990-01-01"),
                new UserVm("katya", "Катя", "1995-05-05")
        ));

        model.addAttribute("accounts", List.of(
                new AccountVm(Currency.RUB, 10_000),
                new AccountVm(Currency.USD, 200),
                new AccountVm(Currency.CNY, 150)
        ));

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
    public String signupPost(@RequestParam String login,
                             @RequestParam String name,
                             @RequestParam String password,
                             @RequestParam String birthdate,
                             Model model) {
        if (login.isBlank() || password.isBlank() || name.isBlank()) {
            model.addAttribute("errors", List.of("Все поля обязательны"));
            return "signup";
        }
        return "redirect:/main?login=" + login;
    }
}

