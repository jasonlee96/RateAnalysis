-- V7: Backfill symbol = 'SGDMYR' for rates inserted after V6 (ELTLoadJob was not setting symbol)
UPDATE ralysis.rates
SET symbol = 'SGDMYR'
WHERE symbol IS NULL OR symbol = '';
