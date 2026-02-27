-- V5: Fix rate_signals column types to match Java double (Hibernate expects DOUBLE PRECISION, not NUMERIC)
ALTER TABLE ralysis.rate_signals
    ALTER COLUMN currentRate TYPE DOUBLE PRECISION,
    ALTER COLUMN hourlyAvg   TYPE DOUBLE PRECISION,
    ALTER COLUMN dailyAvg    TYPE DOUBLE PRECISION,
    ALTER COLUMN dailyP90    TYPE DOUBLE PRECISION;
