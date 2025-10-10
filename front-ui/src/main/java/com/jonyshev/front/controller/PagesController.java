package com.jonyshev.front.controller;

import com.jonyshev.commons.model.Currency;
import com.jonyshev.front.client.AccountsClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PagesController {

    private final AccountsClient accountsClient;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @GetMapping("/")
    public String root() {
        return "redirect:/main";
    }

    @GetMapping("/main")
    public String main(Authentication auth, Model model) {
        String login = (auth != null ? auth.getName() : "guest");
        var userProfile = accountsClient.getUserProfile(login);
        var listUserProfile = accountsClient.getListUserProfile();

        model.addAttribute("login", userProfile.getLogin());
        model.addAttribute("name", userProfile.getName());
        model.addAttribute("birthdate", userProfile.getBirthdate());

        model.addAttribute("accounts", userProfile.getAccounts());

        model.addAttribute("users", listUserProfile);

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
                             HttpServletRequest request,
                             HttpServletResponse response,
                             Model model) {
        if (login.isBlank() || password.isBlank() || name.isBlank() || birthdate.isBlank()) {
            model.addAttribute("errors", List.of("Все поля обязательны"));
            return "signup";
        }

        try {
            LocalDate bd = LocalDate.parse(birthdate);
            if (Period.between(bd, LocalDate.now()).getYears() < 18) {
                model.addAttribute("errors", java.util.List.of("age_restriction"));
                return "signup";
            }
        } catch (Exception e) {
            model.addAttribute("errors", java.util.List.of("invalid_birthdate"));
            return "signup";
        }

        boolean ok = accountsClient.createUser(login, name, password, birthdate);
        if (!ok) {
            model.addAttribute("errors", List.of("Не удалось создать пользователя (возможно, логин занят)"));
            return "signup";
        }
        var authReq = new UsernamePasswordAuthenticationToken(login, password);
        var auth = authenticationManager.authenticate(authReq);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);
        return "redirect:/main";
    }
}

