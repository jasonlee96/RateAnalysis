-- V2: Add symbol column to rates table for multi-symbol support
ALTER TABLE ralysis.rates
    ADD COLUMN IF NOT EXISTS symbol VARCHAR(20);

CREATE INDEX IF NOT EXISTS idx_rates_symbol
    ON ralysis.rates (symbol);

CREATE INDEX IF NOT EXISTS idx_rates_symbol_date
    ON ralysis.rates (symbol, dateRetrieved DESC);
