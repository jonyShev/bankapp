package com.jonyshev.commons.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Currency {
    RUB("Российский рубль"),
    USD("Доллар США"),
    CNY("Китайский юань");

    private final String title;
}
