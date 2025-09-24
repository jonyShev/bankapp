package com.jonyshev.exchange.repository;

import com.jonyshev.exchange.model.RateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RateRepository extends JpaRepository<RateEntity, String> {
}
