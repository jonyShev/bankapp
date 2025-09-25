package com.jonyshev.accounts.repository;

import com.jonyshev.accounts.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByLogin(String login);
}
