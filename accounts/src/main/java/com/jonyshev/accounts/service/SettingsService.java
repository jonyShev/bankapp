package com.jonyshev.accounts.service;

import com.jonyshev.accounts.model.AccountEntity;
import com.jonyshev.accounts.repository.AccountRepository;
import com.jonyshev.accounts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Transactional
    public void changePassword(String login, String password, String confirm) {
        if (password == null || password.isBlank()) throw new IllegalArgumentException("empty_password");
        if (!password.equals(confirm)) throw new IllegalArgumentException("mismatch");
        var userEntity = userRepository.findByLogin(login).orElseThrow();
        userEntity.setPassHash(encoder.encode(password));
        userRepository.save(userEntity);
    }

    @Transactional
    public void updateProfile(String login, String name, String birthdate) {
        var userEntity = userRepository.findByLogin(login).orElseThrow();

        boolean changed = false;

        if (name != null) {
            var nm = name.trim();
            if (!nm.isEmpty() && !nm.equals(userEntity.getName())) {
                userEntity.setName(nm);
                changed = true;
            }
        }

        if (birthdate != null) {
            var bdStr = birthdate.trim();
            if (!bdStr.isEmpty()) {
                LocalDate bd;
                try {
                    bd = LocalDate.parse(bdStr);
                } catch (Exception e) {
                    throw new IllegalArgumentException("invalid_birthdate");
                }

                if (java.time.Period.between(bd, LocalDate.now()).getYears() < 18)
                    throw new IllegalArgumentException("age_restriction");

                if (!bd.equals(userEntity.getBirthdate())) {
                    userEntity.setBirthdate(bd);
                    changed = true;
                }
            }
        }

        if (changed) {
            userRepository.save(userEntity);
        }
    }

    @Transactional
    public void reconcileAccounts(String login, List<String> wantedCurrencies) {
        var u = userRepository.findByLogin(login).orElseThrow();

        var want = Optional.ofNullable(wantedCurrencies).orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        try {
            for (var code : want) com.jonyshev.commons.model.Currency.valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid_currency");
        }

        var existing = accountRepository.findAllByUserId(u.getId());
        var existingCodes = existing.stream().map(AccountEntity::getCurrency).collect(Collectors.toSet());

        if (existingCodes.equals(want)) return;

        for (var code : want) {
            if (!existingCodes.contains(code)) {
                var e = new AccountEntity();
                e.setUserId(u.getId());
                e.setCurrency(code);
                e.setValue(BigDecimal.ZERO);
                e.setUpdatedAt(OffsetDateTime.now());
                accountRepository.save(e);
            }
        }

        for (var a : existing) {
            if (!want.contains(a.getCurrency())) {
                if (a.getValue().signum() != 0)
                    throw new IllegalArgumentException("non_zero_account");
                accountRepository.delete(a);
            }
        }
    }
}