package com.jonyshev.accounts.controller;

import com.jonyshev.accounts.service.AccountService;
import com.jonyshev.commons.dto.UserCreateRequest;
import com.jonyshev.commons.dto.UserProfileDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AccountsController {

    private final AccountService accountService;

    @PostMapping("/users")
    public ResponseEntity<Void> create(@Valid @RequestBody UserCreateRequest request) {
        accountService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/users/{login}")
    public UserProfileDto profile(@PathVariable String login) {
        return accountService.getProfile(login);
    }

    @PostMapping("/internal/auth")
    public ResponseEntity<Void> auth(@RequestParam String login, @RequestParam String password) {
        return accountService.checkPassword(login, password) ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/users")
    public List<UserProfileDto> getAllUserProfile() {
        return accountService.getAllUserProfile();
    }
}
