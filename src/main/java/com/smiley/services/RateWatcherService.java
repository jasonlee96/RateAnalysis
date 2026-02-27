package com.smiley.services;

import com.smiley.entities.RateSignalEntity;
import com.smiley.models.RateWindowStats;
import com.smiley.models.WatcherConfig;
import com.smiley.models.enums.RateSignalEnum;
import com.smiley.repositories.RateRepository;
import com.smiley.repositories.RateSignalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class RateWatcherService {

    private final RateRepository _rateRepository;
    private final RateSignalRepository _signalRepository;
    private final NotifyService _notifyService;

    public RateWatcherService(RateRepository rateRepository, RateSignalRepository signalRepository, NotifyService notifyService) {
        _rateRepository = rateRepository;
        _signalRepository = signalRepository;
        _notifyService = notifyService;
    }

    public CompletableFuture<Boolean> evaluateSymbol(String symbol, WatcherConfig config) {
        var latestRate = _rateRepository.getLatestRateBySymbolAsync(symbol).join();
        if (latestRate == null) {
            log.info("No rate data found for symbol {}", symbol);
            return CompletableFuture.completedFuture(true);
        }

        double currentRate = latestRate.getRate();
        var hourlyStats = _rateRepository.getHourlyStatsAsync(symbol, config.getHourlyWindow()).join();
        var dailyStats = _rateRepository.getDailyStatsAsync(symbol, config.getDailyWindow()).join();

        var signal = evaluate(currentRate, hourlyStats, dailyStats, config);
        log.info("Symbol: {}, Rate: {}, Signal: {}", symbol, currentRate, signal);

        var entity = new RateSignalEntity();
        entity.setSymbol(symbol);
        entity.setSignalDate(Instant.now());
        entity.setCurrentRate(currentRate);
        entity.setHourlyAvg(hourlyStats.getAvg());
        entity.setDailyAvg(dailyStats.getAvg());
        entity.setDailyP90(dailyStats.getP90());
        entity.setSignalType(signal.name());

        if (signal != RateSignalEnum.NORMAL) {
            _notifyService.send(String.format(
                "[RateWatcher] %s signal for %s | Rate: %.4f | HourlyAvg: %.4f | DailyAvg: %.4f | DailyP90: %.4f",
                signal.name(), symbol, currentRate, hourlyStats.getAvg(), dailyStats.getAvg(), dailyStats.getP90()
            ));
        }

        return _signalRepository.saveSignalAsync(entity);
    }

    private RateSignalEnum evaluate(double currentRate, RateWindowStats hourly, RateWindowStats daily, WatcherConfig config) {
        if (daily.getCount() > 0) {
            if (currentRate > daily.getP90()) {
                return RateSignalEnum.DAILY_HIGH;
            }
            if (currentRate > daily.getAvg() * config.getHighThreshold()) {
                return RateSignalEnum.DAILY_HIGH;
            }
            if (currentRate < daily.getAvg() * config.getLowThreshold()) {
                return RateSignalEnum.DAILY_LOW;
            }
        }
        if (hourly.getCount() > 0) {
            if (currentRate > hourly.getAvg() * config.getHighThreshold()) {
                return RateSignalEnum.HOURLY_HIGH;
            }
            if (currentRate < hourly.getAvg() * config.getLowThreshold()) {
                return RateSignalEnum.HOURLY_LOW;
            }
        }
        return RateSignalEnum.NORMAL;
    }
}
