package com.smiley.services;

import com.smiley.entities.JobInfoEntity;
import com.smiley.models.enums.JobTypeEnum;
import com.smiley.models.enums.StatusEnum;
import com.smiley.repositories.interfaces.IJobInfoRepository;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class JobService {

    private final IJobInfoRepository _repository;
    public JobService(IJobInfoRepository repository){
        _repository = repository;
    }
    // check job is ready to execute
    public CompletableFuture<JobInfoEntity> IsJobReadyToProceedAsync(JobTypeEnum job){
        var jobTask = _repository.getJobInfoByIDAsync(job);
        var jobInfo = jobTask.join();

        if(jobInfo == null){
            return CompletableFuture.completedFuture(null);
        }

        if(jobInfo.getJobStatusID() == StatusEnum.Pending.getStatusID()){
            return CompletableFuture.completedFuture(null);
        }

        var currentUtc = Instant.now();
        if(jobInfo.getNextJobAt().isAfter(currentUtc)){
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.completedFuture(jobInfo);
    }
    // update job to Pending / Success (if success, update next job date)
    public CompletableFuture<Boolean> UpdateJobInfoAsync(JobTypeEnum job, StatusEnum status, Optional<LocalDate> jobDate, Optional<Instant> nextJobDate){
        return CompletableFuture.supplyAsync(() -> {
            var jobData = _repository.getJobInfoByIDAsync(job).join();
            jobData.setJobStatusID(status.getStatusID());

            if(status == StatusEnum.Success) {
                jobData.setJobValue(jobDate.orElseGet(jobData::getJobValue));
                jobData.setNextJobAt(nextJobDate.orElseGet(jobData::getNextJobAt));
            }

            var updateTask = _repository.updateJobInfoAsync(jobData);
            return updateTask.join(); // Simulate success
        });
    }

    public CompletableFuture<Boolean> CheckJobDependency(LocalDate date, JobTypeEnum... jobs){
        return CompletableFuture.supplyAsync(() -> {
            var isProceed = false;
           for(var job : jobs){
               var jobData = _repository.getJobInfoByIDAsync(job).join();

               if(jobData != null){
                    if(!date.isAfter(jobData.getJobValue())){
                        isProceed = true;
                   }
               }else{
                   break;
               }
           }
           return isProceed;
        });
    }

}
