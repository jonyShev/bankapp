package com.jonyshev.exchange.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "rates")
@Getter
@Setter
public class RateEntity {
    @Id
    @Column(length = 8, nullable = false)
    private String code;

    @Column(length = 64, nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal value;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
