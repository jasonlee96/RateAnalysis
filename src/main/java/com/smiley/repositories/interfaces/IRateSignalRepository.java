package com.smiley.repositories.interfaces;

import com.smiley.entities.RateSignalEntity;

import java.util.concurrent.CompletableFuture;

public interface IRateSignalRepository {
    CompletableFuture<Boolean> saveSignalAsync(RateSignalEntity signal);
}
