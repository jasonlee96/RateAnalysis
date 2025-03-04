package com.smiley.repositories;

import com.smiley.entities.JobInfoEntity;
import com.smiley.models.enums.JobTypeEnum;
import com.smiley.repositories.interfaces.IJobInfoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Repository
public class JobInfoRepository implements IJobInfoRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Async
    public CompletableFuture<JobInfoEntity> getJobInfoByIDAsync(JobTypeEnum job) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<JobInfoEntity> query = cb.createQuery(JobInfoEntity.class);
        Root<JobInfoEntity> root = query.from(JobInfoEntity.class);

        Predicate finalPredicate = cb.equal(root.get("jobTypeID"), job.getJobTypeID());

        query.select(root).where(finalPredicate);

        return CompletableFuture.completedFuture(entityManager.createQuery(query).getSingleResult());
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<Boolean> updateJobInfoAsync(JobInfoEntity data) {
        JobInfoEntity job = entityManager.find(JobInfoEntity.class, data.getId());
        if (job != null) {
            if (job.getJobStatusID() != data.getJobStatusID()) job.setJobStatusID(data.getJobStatusID());
            if (!job.getNextJobAt().equals(data.getNextJobAt())) job.setNextJobAt(data.getNextJobAt());
            if (!job.getJobValue().equals(data.getJobValue())) job.setJobValue(data.getJobValue());

            entityManager.merge(job); // ✅ Ensure entity is managed
        }
        return CompletableFuture.completedFuture(true);
    }
}
