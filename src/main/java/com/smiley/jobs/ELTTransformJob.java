package com.smiley.jobs;

import com.smiley.common.AppSetting;
import com.smiley.helpers.DateHelper;
import com.smiley.models.enums.JobTypeEnum;
import com.smiley.models.enums.StatusEnum;
import com.smiley.services.JobService;
import com.smiley.services.RateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j // Lombok auto-generates an SLF4J logger
@Service
public class ELTTransformJob {

    private final RateService _service;
    private final JobService _jobService;
    private final AppSetting _appSetting;

    public ELTTransformJob(RateService service, JobService jobService, AppSetting appSetting) {
        this._service = service;
        this._jobService = jobService;
        this._appSetting = appSetting;
    }

    @Scheduled(fixedRate = 30 * 60 * 1000) // Runs every 30 minutes (in milliseconds)
    public void executeBackgroundTask() {
        executeBackgroundTaskAsync();
    }

    @Async
    public void executeBackgroundTaskAsync() {
        log.info("Running ELT_TRANSFORM task...");
        var jobType = JobTypeEnum.ELT_TRANSFORM;
        try {
            var jobInfo = _jobService.IsJobReadyToProceedAsync(jobType).join();
            if(jobInfo == null){
                log.info("ELT Transform is not ready to proceed");
                return;
            }

            ZoneId zone = ZoneId.systemDefault();
            // Convert Instant to LocalDate in that time zone
            var currentDate = jobInfo.getNextJobAt().atZone(zone).toLocalDate();
            //TODO job dependency
            var isProceed = _jobService.CheckJobDependency(currentDate, JobTypeEnum.ELT_LOAD).join();
            if(!isProceed) {
                log.info("ELT Load job for date {} is not completed", currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                return;
            }
            var result = _service.ELTTransform(currentDate).join();

            if(result){
                // update success and form next job
                var zoneOffset = ZoneId.systemDefault().getRules().getOffset(Instant.now());
                var nextJobAt = LocalDateTime.of(DateHelper.AddDays(currentDate, 1), LocalTime.of(19,30,0)).toInstant(zoneOffset);

                var updateResult = _jobService.UpdateJobInfoAsync(jobType, StatusEnum.Success, Optional.of(currentDate), Optional.of(nextJobAt)).join();
                if(!updateResult) log.error("Update Job Failed");
            }
            else{
                // update fail and retry
                var updateResult = _jobService.UpdateJobInfoAsync(jobType, StatusEnum.Failed, Optional.of(currentDate), Optional.empty()).join();
                if(!updateResult) log.error("Update Job Failed");
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage());
        }
        log.info("ELT_TRANSFORM task completed.");
    }
}