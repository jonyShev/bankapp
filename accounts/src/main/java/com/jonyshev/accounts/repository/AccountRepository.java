package com.jonyshev.accounts.repository;

import com.jonyshev.accounts.model.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    List<AccountEntity> findAllByUserId(UUID userId);
    Optional<AccountEntity> findByUserIdAndCurrency(UUID userId, String currency);
}
