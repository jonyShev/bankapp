package com.jonyshev.accounts.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserProfileDto {
    private String login;
    private String name;
    private String birthdate;
    private List<AccountDto> accounts;

    @Data
    public static class AccountDto {
        private String currency;
        private String value;
    }
}