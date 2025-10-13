package com.jonyshev.accounts.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordChangeRequest {
    @NotBlank
    private String login;
    @NotBlank
    private String password;
    @NotBlank
    private String confirm;
}