package com.smiley.repositories.interfaces;

import com.smiley.entities.RateEntity;
import com.smiley.models.RateWindowStats;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IRateRepository {
    CompletableFuture<List<RateEntity>> getRatesByDateAsync(Date date);
    CompletableFuture<Boolean> insertRatesAsync(List<RateEntity> rates);
    CompletableFuture<Boolean> aggregateRateByDaily(LocalDate targetDate);
    CompletableFuture<Boolean> aggregateRateByMonthly(LocalDate dateFrom, LocalDate dateTo);
    CompletableFuture<RateEntity> getLatestRateBySymbolAsync(String symbol);
    CompletableFuture<RateWindowStats> getHourlyStatsAsync(String symbol, int hours);
    CompletableFuture<RateWindowStats> getDailyStatsAsync(String symbol, int days);
}
