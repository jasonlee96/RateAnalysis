package com.smiley.repositories;

import com.smiley.entities.RateSignalEntity;
import com.smiley.repositories.interfaces.IRateSignalRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Repository
public class RateSignalRepository implements IRateSignalRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Async
    @Transactional
    public CompletableFuture<Boolean> saveSignalAsync(RateSignalEntity signal) {
        try {
            entityManager.persist(signal);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }
    }
}
