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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(String login) {
        var userEntity = userRepository.findByLogin(login).orElseThrow();
        var accountEntityList = accountRepository.findAllByUserId(userEntity.getId());

        var byCode = new HashMap<String, AccountEntity>();
        for (var accountEntity : accountEntityList) byCode.put(accountEntity.getCurrency(), accountEntity);

        var accountDtos = new ArrayList<UserProfileDto.AccountDto>();
        for (var currency : Currency.values()) {
            var accountDto = new UserProfileDto.AccountDto();
            accountDto.setCurrency(currency);
            var found = byCode.get(currency.name());
            if (found != null) {
                accountDto.setExists(true);
                accountDto.setValue(found.getValue().setScale(2, RoundingMode.HALF_UP)); // аккуратно отформатируем
            } else {
                accountDto.setExists(false);
                accountDto.setValue(BigDecimal.ZERO.setScale(2));
            }
            accountDtos.add(accountDto);
        }

        return UserProfileDto.builder()
                .login(userEntity.getLogin())
                .name(userEntity.getName())
                .birthdate(userEntity.getBirthdate().toString())
                .accounts(accountDtos)
                .build();
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
