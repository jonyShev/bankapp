package com.jonyshev.front.model;


import com.jonyshev.commons.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;

public class ViewModels {
    @Data
    @AllArgsConstructor
    public static class UserVm {
        private String login;
        private String name;
        private String birthdate;
    }

    @Data
    @AllArgsConstructor
    public static class AccountVm {
        private Currency currency;
        private double value;

        public boolean isExists() {
            return value > 0;
        }
    }
}
