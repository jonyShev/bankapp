CREATE TABLE IF NOT EXISTS rates (
    code       VARCHAR(8)  PRIMARY KEY,     -- RUB, USD...
    name       VARCHAR(64) NOT NULL,        -- Русское название
    value      NUMERIC(12,4) NOT NULL,      -- курс к базовой валюте (RUB = 1)
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- стартовые значения
INSERT INTO rates(code, name, value)
VALUES
 ('RUB', 'Российский рубль', 1.0000),
 ('USD', 'Доллар США',       90.0000),
 ('CNY', 'Китайский юань',   12.0000)
ON CONFLICT (code) DO NOTHING;