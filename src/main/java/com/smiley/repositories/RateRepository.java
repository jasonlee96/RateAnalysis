package com.smiley.repositories;
import com.smiley.common.AppSetting;
import com.smiley.entities.*;
import com.smiley.helpers.DateHelper;
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

