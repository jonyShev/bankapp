package com.jonyshev.accounts.controller;

import com.jonyshev.accounts.model.AccountsUpdateRequest;
import com.jonyshev.accounts.model.PasswordChangeRequest;
import com.jonyshev.accounts.model.ProfileUpdateRequest;
import com.jonyshev.accounts.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal")
public class InternalSettingsController {

    private final SettingsService settingsService;

    @PostMapping("/password")
    public ResponseEntity<?> password(@RequestBody PasswordChangeRequest passwordChangeRequest) {
        try {
            settingsService.changePassword(passwordChangeRequest.getLogin(), passwordChangeRequest.getPassword(), passwordChangeRequest.getConfirm());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/profile")
    public ResponseEntity<?> profile(@RequestBody ProfileUpdateRequest request) {
        try {
            settingsService.updateProfile(request.getLogin(), request.getName(), request.getBirthdate());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/accounts")
    public ResponseEntity<?> accounts(@RequestBody AccountsUpdateRequest request) {
        try {
            settingsService.reconcileAccounts(request.getLogin(), request.getAccounts());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
