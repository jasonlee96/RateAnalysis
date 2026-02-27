-- V3: Create rate_signals table for watcher output
CREATE TABLE IF NOT EXISTS ralysis.rate_signals (
    id          BIGSERIAL PRIMARY KEY,
    symbol      VARCHAR(20)              NOT NULL,
    signalDate  TIMESTAMP WITH TIME ZONE NOT NULL,
    currentRate DECIMAL(18, 6)           NOT NULL,
    hourlyAvg   DECIMAL(18, 6),
    dailyAvg    DECIMAL(18, 6),
    dailyP90    DECIMAL(18, 6),
    signalType  VARCHAR(20)              NOT NULL,
    createdAt   TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rate_signals_symbol_date
    ON ralysis.rate_signals (symbol, signalDate DESC);
