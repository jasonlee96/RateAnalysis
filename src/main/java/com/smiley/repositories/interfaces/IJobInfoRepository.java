package com.smiley.repositories.interfaces;

import com.smiley.entities.JobInfoEntity;
import com.smiley.models.enums.JobTypeEnum;

import java.util.concurrent.CompletableFuture;

public interface IJobInfoRepository {
    CompletableFuture<JobInfoEntity> getJobInfoByIDAsync(JobTypeEnum job);
    CompletableFuture<Boolean> updateJobInfoAsync(JobInfoEntity job);

}
