package com.smiley.repositories;
import com.smiley.common.AppSetting;
import com.smiley.entities.*;
import com.smiley.helpers.DateHelper;
import com.smiley.models.RateWindowStats;
import com.smiley.repositories.interfaces.IRateRepository;
import jakarta.persistence.criteria.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Repository
public class RateRepository implements IRateRepository {
    @PersistenceContext
    private EntityManager entityManager;
    private final AppSetting _appSetting;

    public RateRepository(AppSetting appSetting){
        _appSetting = appSetting;
    }

    @Async
    @Override
    public CompletableFuture<List<RateEntity>> getRatesByDateAsync(Date date) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RateEntity> query = cb.createQuery(RateEntity.class);
        Root<RateEntity> root = query.from(RateEntity.class);

        Predicate finalPredicate = cb.and(
                cb.greaterThanOrEqualTo(root.get("createdAt"), date),
                cb.lessThan(root.get("createdAt"), DateHelper.AddDays(date, 1))
        );

        query.select(root).where(finalPredicate);

        return CompletableFuture.completedFuture(entityManager.createQuery(query).getResultList());
    }


    @Override
    @Async
    @Transactional
    public CompletableFuture<Boolean> insertRatesAsync(List<RateEntity> rates) {
        try {
            insertRatesBatch(rates);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<Boolean> aggregateRateByDaily(LocalDate targetDate) {
        String rawQuery = """
            INSERT INTO ralysis.rates_agg_daily(date, rateAvg, rate50Pct, rate90Pct, rateMax, rateMin, createdAt, updatedAt)
            SELECT
                dateRetrieved::timestamp::date as date,
                avg(rate) as rateAvg,
                percentile_cont(0.5) within GROUP(order by rate) as rate50Pct,
                percentile_cont(0.9) within GROUP(order by rate) as rate90Pct,
                max(rate) as rateMax,
                min(rate) as rateMin,
                NOW() as createdAt,
                NOW() as updatedAt
            FROM ralysis.rates
            WHERE dateRetrieved >= :dateFrom AND dateRetrieved < :dateTo
            GROUP BY date
            ON CONFLICT (date) DO UPDATE
            SET
                rateAvg = EXCLUDED.rateAvg,
                rate50Pct = EXCLUDED.rate50Pct,
                rate90Pct = EXCLUDED.rate90Pct,
                rateMax = EXCLUDED.rateMax,
                rateMin = EXCLUDED.rateMin,
                updatedAt = NOW();
            """;
        var query = entityManager.createNativeQuery(rawQuery);
        query.setParameter("dateFrom", targetDate);
        query.setParameter("dateTo", DateHelper.AddDays(targetDate,1));

        var i = query.executeUpdate();
        return CompletableFuture.completedFuture(true);
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<Boolean> aggregateRateByMonthly(LocalDate dateFrom, LocalDate dateTo) {
        String rawQuery = """
            INSERT INTO ralysis.rates_agg_monthly(date, rateAvg, rate50Pct, rate90Pct, rateMax, rateMin, createdAt, updatedAt)
            SELECT
                DATE_TRUNC('month', dateRetrieved)::DATE as date,
                avg(rate) as rateAvg,
                percentile_cont(0.5) within GROUP(order by rate) as rate50Pct,
                percentile_cont(0.9) within GROUP(order by rate) as rate90Pct,
                max(rate) as rateMax,
                min(rate) as rateMin,
                NOW() as createdAt,
                NOW() as updatedAt
            FROM ralysis.rates
            WHERE dateRetrieved >= :dateFrom AND dateRetrieved < :dateTo
            GROUP BY date
            ON CONFLICT (date) DO UPDATE
            SET
                rateAvg = EXCLUDED.rateAvg,
                rate50Pct = EXCLUDED.rate50Pct,
                rate90Pct = EXCLUDED.rate90Pct,
                rateMax = EXCLUDED.rateMax,
                rateMin = EXCLUDED.rateMin,
                updatedAt = NOW();
        """;
        var query = entityManager.createNativeQuery(rawQuery);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateTo", dateTo);

        var i = query.executeUpdate();
        return CompletableFuture.completedFuture(true);
    }

    @Override
    @Async
    public CompletableFuture<RateEntity> getLatestRateBySymbolAsync(String symbol) {
        String rawQuery = """
            SELECT * FROM ralysis.rates
            WHERE symbol = :symbol
            ORDER BY dateRetrieved DESC
            LIMIT 1
            """;
        var query = entityManager.createNativeQuery(rawQuery, RateEntity.class);
        query.setParameter("symbol", symbol);
        var result = query.getResultList();
        return CompletableFuture.completedFuture(result.isEmpty() ? null : (RateEntity) result.get(0));
    }

    @Override
    @Async
    public CompletableFuture<RateWindowStats> getHourlyStatsAsync(String symbol, int hours) {
        String rawQuery = """
            SELECT
                COALESCE(AVG(rate), 0),
                COALESCE(STDDEV(rate), 0),
                COALESCE(PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY rate), 0),
                COALESCE(MAX(rate), 0),
                COALESCE(MIN(rate), 0),
                COUNT(*)
            FROM ralysis.rates
            WHERE symbol = :symbol
            AND dateRetrieved >= NOW() - CAST(:hours || ' hours' AS INTERVAL)
            """;
        var query = entityManager.createNativeQuery(rawQuery);
        query.setParameter("symbol", symbol);
        query.setParameter("hours", hours);
        return CompletableFuture.completedFuture(mapToWindowStats((Object[]) query.getSingleResult()));
    }

    @Override
    @Async
    public CompletableFuture<RateWindowStats> getDailyStatsAsync(String symbol, int days) {
        String rawQuery = """
            SELECT
                COALESCE(AVG(rate), 0),
                COALESCE(STDDEV(rate), 0),
                COALESCE(PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY rate), 0),
                COALESCE(MAX(rate), 0),
                COALESCE(MIN(rate), 0),
                COUNT(*)
            FROM ralysis.rates
            WHERE symbol = :symbol
            AND dateRetrieved >= NOW() - CAST(:days || ' days' AS INTERVAL)
            """;
        var query = entityManager.createNativeQuery(rawQuery);
        query.setParameter("symbol", symbol);
        query.setParameter("days", days);
        return CompletableFuture.completedFuture(mapToWindowStats((Object[]) query.getSingleResult()));
    }

    private RateWindowStats mapToWindowStats(Object[] row) {
        var stats = new RateWindowStats();
        stats.setAvg(((Number) row[0]).doubleValue());
        stats.setStddev(((Number) row[1]).doubleValue());
        stats.setP90(((Number) row[2]).doubleValue());
        stats.setMax(((Number) row[3]).doubleValue());
        stats.setMin(((Number) row[4]).doubleValue());
        stats.setCount(((Number) row[5]).intValue());
        return stats;
    }

    public void insertRatesBatch(List<RateEntity> rates) {
        int batchSize = _appSetting.getBatchSize(); // Adjust as needed

        for (int i = 0; i < rates.size(); i++) {
            entityManager.persist(rates.get(i)); // Save entity

            // Avoid flushing on i = 0
            if (i > 0 && i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.flush(); // Ensure any remaining entities are saved
        entityManager.clear();
    }
}

