-- V1: Initial schema
-- Safe to run on existing databases (IF NOT EXISTS throughout)

CREATE SCHEMA IF NOT EXISTS ralysis;

CREATE TABLE IF NOT EXISTS ralysis.rates (
    id            BIGSERIAL PRIMARY KEY,
    dateRetrieved TIMESTAMP WITH TIME ZONE,
    rate          DECIMAL(18, 6),
    createdAt     TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS ralysis.rates_agg_daily (
    id        BIGSERIAL PRIMARY KEY,
    date      DATE UNIQUE,
    rateAvg   DECIMAL(18, 6),
    rate50Pct DECIMAL(18, 6),
    rate90Pct DECIMAL(18, 6),
    rateMax   DECIMAL(18, 6),
    rateMin   DECIMAL(18, 6),
    createdAt TIMESTAMP WITH TIME ZONE,
    updatedAt TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS ralysis.rates_agg_monthly (
    id        BIGSERIAL PRIMARY KEY,
    date      DATE UNIQUE,
    rateAvg   DECIMAL(18, 6),
    rate50Pct DECIMAL(18, 6),
    rate90Pct DECIMAL(18, 6),
    rateMax   DECIMAL(18, 6),
    rateMin   DECIMAL(18, 6),
    createdAt TIMESTAMP WITH TIME ZONE,
    updatedAt TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS ralysis.jobinfos (
    id           BIGSERIAL PRIMARY KEY,
    jobTypeID    INT,
    jobValue     DATE,
    jobStatusID  INT,
    nextJobAt    TIMESTAMP WITH TIME ZONE,
    createdAt    TIMESTAMP WITH TIME ZONE,
    updatedAt    TIMESTAMP WITH TIME ZONE
);
