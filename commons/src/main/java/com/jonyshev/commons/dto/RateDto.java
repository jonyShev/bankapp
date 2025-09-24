package com.jonyshev.commons.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateDto {
    private String title;
    private String name;
    private BigDecimal value;
}
