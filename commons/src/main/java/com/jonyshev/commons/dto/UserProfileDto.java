package com.jonyshev.commons.dto;

import com.jonyshev.commons.model.Currency;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UserProfileDto {
    private String login;
    private String name;
    private String birthdate;
    private List<AccountDto> accounts;

    @Data
    public static class AccountDto {
        private Currency currency;
        private BigDecimal value;

        public boolean isExists() {
            return value != null && value.signum() > 0;
        }
    }
}