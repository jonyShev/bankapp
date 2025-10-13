package com.jonyshev.accounts.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @NotBlank
    private String login;
    @NotBlank
    private String name;
    @NotBlank
    private String birthdate;
}