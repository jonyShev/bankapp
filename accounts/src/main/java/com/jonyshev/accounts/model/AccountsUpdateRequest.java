package com.jonyshev.accounts.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AccountsUpdateRequest {
    @NotBlank
    private String login;
    private List<String> accounts;
}