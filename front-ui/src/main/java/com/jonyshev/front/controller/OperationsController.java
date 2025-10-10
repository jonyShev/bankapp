package com.jonyshev.front.controller;

import com.jonyshev.front.client.CashClient;
import com.jonyshev.front.client.TransferClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OperationsController {

    private final CashClient cashClient;
    private final TransferClient transferClient;

    @PostMapping("/user/{login}/cash")
    public String cashOp(@PathVariable String login,
                         @RequestParam String action,
                         @RequestParam String currency,
                         @RequestParam String amount,
                         Authentication auth,
                         RedirectAttributes redirectAttributes) {
        if (!isOwn(auth, login)) {
            redirectAttributes.addFlashAttribute("cashErrors", java.util.List.of("not_allowed"));
            return "redirect:/main";
        }

        if (amount == null || !amount.matches("\\d+(\\.\\d{1,2})?")) {
            redirectAttributes.addFlashAttribute("cashErrors", java.util.List.of("invalid_amount"));
            return "redirect:/main";
        }

        String err = switch (action) {
            case "deposit" -> cashClient.deposit(login, currency, amount);
            case "withdraw" -> cashClient.withdraw(login, currency, amount);
            default -> "unknown_action";
        };
        if (err != null && !err.isBlank()) {
            redirectAttributes.addFlashAttribute("cashErrors", java.util.List.of(err));
        }

        return "redirect:/main";
    }

    @PostMapping("/user/{login}/transfer/self")
    public String transferSelf(@PathVariable String login,
                               @RequestParam String from,
                               @RequestParam String to,
                               @RequestParam String amount,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        if (!isOwn(auth, login)) {
            redirectAttributes.addFlashAttribute("transferErrors", List.of("not_allowed"));
            return "redirect:/main";
        }

        String err = transferClient.transferSelf(login, from, to, amount);

        if (err != null && !err.isBlank()) {
            redirectAttributes.addFlashAttribute("transferErrors", List.of(err));
        }
        if (err != null && !err.isBlank()) {
            redirectAttributes.addFlashAttribute("transferErrors", List.of(err));
        }
        return "redirect:/main";
    }

    @PostMapping("/user/{login}/transfer/to")
    public String transferTo(@PathVariable String login,
                             @RequestParam String toLogin,
                             @RequestParam String from,
                             @RequestParam String to,
                             @RequestParam String amount,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        if (!isOwn(auth, login)) {
            redirectAttributes.addFlashAttribute("transferOtherErrors", List.of("not_allowed"));
            return "redirect:/main";
        }

        String err = transferClient.transferToOther(login, toLogin, from, to, amount);

        if (err != null && !err.isBlank()) {
            redirectAttributes.addFlashAttribute("transferOtherErrors", List.of(err));
        }
        return "redirect:/main";
    }

    private boolean isOwn(Authentication auth, String login) {
        return auth != null && auth.getName().equals(login);
    }
}
