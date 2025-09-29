package com.jonyshev.accounts.service;

import com.jonyshev.accounts.model.AccountEntity;
import com.jonyshev.accounts.model.UserEntity;
import com.jonyshev.accounts.repository.AccountRepository;
import com.jonyshev.accounts.repository.UserRepository;
import com.jonyshev.commons.dto.UserCreateRequest;
import com.jonyshev.commons.dto.UserProfileDto;
import com.jonyshev.commons.model.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder encoder;

    @Transactional
    public void createUser(UserCreateRequest request) {
        userRepository.findByLogin(request.getLogin()).ifPresent(u -> {
            throw new IllegalArgumentException("login_already_exist");
        });

        var userEntity = new UserEntity();
        userEntity.setLogin(request.getLogin());
        userEntity.setName(request.getName());
        userEntity.setBirthdate(LocalDate.parse(request.getBirthdate()));
        userEntity.setPassHash(encoder.encode(request.getPassword()));
        userRepository.save(userEntity);

        for (var c : Currency.values()) {
            var accountEntity = new AccountEntity();
            accountEntity.setUserId(userEntity.getId());
            accountEntity.setCurrency(c.name());
            accountEntity.setValue(BigDecimal.ZERO);
            accountEntity.setUpdatedAt(OffsetDateTime.now());
            accountRepository.save(accountEntity);
        }
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(String login) {
        var userEntity = userRepository.findByLogin(login).orElseThrow();
        var accountEntityList = accountRepository.findAllByUserId(userEntity.getId());
        return UserProfileDto.builder()
                .login(userEntity.getLogin())
                .name(userEntity.getName())
                .birthdate(userEntity.getBirthdate().toString())
                .accounts(accountEntityList.stream().map(a -> {
                    var accountDto = new UserProfileDto.AccountDto();
                    accountDto.setCurrency(Currency.valueOf(a.getCurrency()));
                    accountDto.setValue(a.getValue());
                    return accountDto;
                }).toList()).build();
    }

    @Transactional(readOnly = true)
    public boolean checkPassword(String login, String password) {
        var userEntity = userRepository.findByLogin(login).orElse(null);
        return userEntity != null && encoder.matches(password, userEntity.getPassHash());
    }

    @Transactional(readOnly = true)
    public List<UserProfileDto> getAllUserProfile() {
        return userRepository.findAll().stream().map(userEntity -> UserProfileDto.builder()
                .login(userEntity.getLogin())
                .name(userEntity.getName())
                .build()).toList();
    }
}
