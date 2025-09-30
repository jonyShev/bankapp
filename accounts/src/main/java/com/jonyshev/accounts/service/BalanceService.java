package com.jonyshev.accounts.service;

import com.jonyshev.accounts.repository.AccountRepository;
import com.jonyshev.accounts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public void add(String login, String currency, BigDecimal amount) {
        var userEntity = userRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("user_not_found"));
        var accountEntity = accountRepository.findByUserIdAndCurrency(userEntity.getId(), currency)
                .orElseThrow(() -> new IllegalArgumentException("account_not_found"));

        accountEntity.setValue(accountEntity.getValue().add(amount));
        accountEntity.setUpdatedAt(OffsetDateTime.now());
        accountRepository.save(accountEntity);
    }

    @Transactional
    public boolean sub(String login, String currency, BigDecimal amount) {
        var userEntity = userRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("user_not_found"));
        var accountEntity = accountRepository.findByUserIdAndCurrency(userEntity.getId(), currency)
                .orElseThrow(() -> new IllegalArgumentException("account_not_found"));
        //check if it's enough money
        if (accountEntity.getValue().compareTo(amount) < 0) {
            return false;
        }
        accountEntity.setValue(accountEntity.getValue().subtract(amount));
        accountEntity.setUpdatedAt(OffsetDateTime.now());
        accountRepository.save(accountEntity);
        return true;
    }
}
