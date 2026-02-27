package com.smiley.jobs;

import com.smiley.common.AppSetting;
import com.smiley.models.enums.JobTypeEnum;
import com.smiley.models.enums.StatusEnum;
import com.smiley.services.JobService;
import com.smiley.services.RateWatcherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
public class RateWatcherJob {

    private final RateWatcherService _watcherService;
    private final JobService _jobService;
    private final AppSetting _appSetting;

    public RateWatcherJob(RateWatcherService watcherService, JobService jobService, AppSetting appSetting) {
        _watcherService = watcherService;
        _jobService = jobService;
        _appSetting = appSetting;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000) // polls every 5 minutes
    public void executeBackgroundTask() {
        executeBackgroundTaskAsync();
    }

    @Async
    public void executeBackgroundTaskAsync() {
        log.info("Running RATE_WATCHER task...");
        var jobType = JobTypeEnum.RATE_WATCHER;
        try {
            var jobInfo = _jobService.IsJobReadyToProceedAsync(jobType).join();
            if (jobInfo == null) {
                log.info("Rate Watcher is not ready to proceed");
                return;
            }

            var symbols = _appSetting.getSymbols();
            if (symbols == null || symbols.isEmpty()) {
                log.info("No symbols configured");
                return;
            }

            boolean allSuccess = true;
            for (var symbol : symbols) {
                if (symbol.getWatcher() == null || !symbol.getWatcher().isEnabled()) continue;
                var result = _watcherService.evaluateSymbol(symbol.getName(), symbol.getWatcher()).join();
                if (!result) {
                    log.error("Watcher evaluation failed for symbol {}", symbol.getName());
                    allSuccess = false;
                }
            }

            var nextJobAt = Instant.now().plusSeconds(5 * 60);
            var status = allSuccess ? StatusEnum.Success : StatusEnum.Failed;
            var updateResult = _jobService.UpdateJobInfoAsync(jobType, status, Optional.empty(), Optional.of(nextJobAt)).join();
            if (!updateResult) log.error("Update Job Failed");

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage());
        }
        log.info("RATE_WATCHER task completed.");
    }
}
