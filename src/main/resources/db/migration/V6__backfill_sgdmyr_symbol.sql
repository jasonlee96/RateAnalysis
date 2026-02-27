-- V6: Backfill symbol = 'SGDMYR' for existing rates rows that predate multi-symbol support
UPDATE ralysis.rates
SET symbol = 'SGDMYR'
WHERE symbol IS NULL OR symbol = '';
