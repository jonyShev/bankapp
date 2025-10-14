package com.jonyshev.front.controller;

import com.jonyshev.front.client.AccountsClient;
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
public class SettingsController {
    private final AccountsClient accounts;

    @PostMapping("/user/{login}/editPassword")
    public String editPassword(@PathVariable String login,
                               @RequestParam String password,
                               @RequestParam String confirm,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.getName().equals(login)) {
            redirectAttributes.addFlashAttribute("passwordErrors", List.of("not_allowed"));
            return "redirect:/main";
        }
        if (password.isBlank() || confirm.isBlank()) {
            redirectAttributes.addFlashAttribute("passwordErrors", List.of("empty_password"));
            return "redirect:/main";
        }
        boolean ok = accounts.changePassword(login, password, confirm);
        if (!ok) redirectAttributes.addFlashAttribute("passwordErrors", List.of("mismatch"));
        return "redirect:/main";
    }

    @PostMapping("/user/{login}/editUserAccounts")
    public String editUserAccounts(@PathVariable String login,
                                   @RequestParam(required = false) String name,
                                   @RequestParam(required = false) String birthdate,
                                   @RequestParam(value = "account", required = false) List<String> accountsWanted,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.getName().equals(login)) {
            redirectAttributes.addFlashAttribute("userAccountsErrors", List.of("not_allowed"));
            return "redirect:/main";
        }

        boolean hasProfileFields =
                (name != null && !name.isBlank()) || (birthdate != null && !birthdate.isBlank());

        if (hasProfileFields) {
            var e1 = accounts.updateProfile(login, name, birthdate);
            if (e1 != null && !e1.isBlank()) {
                redirectAttributes.addFlashAttribute("userAccountsErrors", List.of(e1));
                return "redirect:/main";
            }
        }

        var e2 = accounts.updateAccounts(login, accountsWanted);
        if (e2 != null && !e2.isBlank()) {
            redirectAttributes.addFlashAttribute("userAccountsErrors", List.of(e2));
        }
        return "redirect:/main";
    }
}
