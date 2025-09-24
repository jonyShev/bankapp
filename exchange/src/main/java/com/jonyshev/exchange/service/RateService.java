package com.jonyshev.exchange.service;

import com.jonyshev.commons.dto.RateDto;
import com.jonyshev.commons.model.Currency;
import com.jonyshev.exchange.model.RateEntity;
import com.jonyshev.exchange.repository.RateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RateService {

    private final RateRepository rateRepository;

    @Transactional(readOnly = true)
    public List<RateDto> getAll() {
        return rateRepository.findAll().stream()
                .map(e -> new RateDto(e.getCode(), e.getName(), e.getValue()))
                .toList();
    }

    @Transactional
    public void upsert(Currency code, BigDecimal value) {
        var rate = rateRepository.findById(code.name()).orElseGet(() -> {
            var rateEntity = new RateEntity();
            rateEntity.setCode(code.name());
            rateEntity.setName(code.getTitle());
            return rateEntity;
        });
        rate.setValue(value);
        rate.setUpdatedAt(OffsetDateTime.now());
        rateRepository.save(rate);
    }
}
