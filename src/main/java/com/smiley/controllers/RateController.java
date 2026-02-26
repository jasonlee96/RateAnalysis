package com.smiley.controllers;

import com.smiley.entities.RateEntity;
import com.smiley.helpers.DateHelper;
import com.smiley.models.RateRequest;
import com.smiley.models.enums.JobTypeEnum;
import com.smiley.models.enums.StatusEnum;
import com.smiley.services.JobService;
import com.smiley.services.RateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api")
public class RateController {

    private final RateService _rateService;
    private final JobService _jobService;

    public RateController(RateService rateService, JobService jobService) {
        this._rateService = rateService;
        this._jobService = jobService;
    }

    @PostMapping("/rates")
    public ResponseEntity<Map<String, Object>> ingestRate(@RequestBody RateRequest request) {
        try {
            var entity = new RateEntity();
            entity.setRate(request.getRate());
            entity.setDateRetrieved(request.getDateRetrieved());

            var inserted = _rateService.insertRates(List.of(entity)).join();
            if (!inserted) {
                return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", "Insert failed"));
            }

            var currentDate = request.getDateRetrieved().atZone(ZoneId.systemDefault()).toLocalDate();
            var zoneOffset = ZoneId.systemDefault().getRules().getOffset(Instant.now());
            var nextJobAt = LocalDateTime.of(DateHelper.AddDays(currentDate, 1), LocalTime.of(9, 0, 0))
                    .toInstant(zoneOffset);

            _jobService.UpdateJobInfoAsync(JobTypeEnum.ELT_LOAD, StatusEnum.Success,
                    Optional.of(currentDate), Optional.of(nextJobAt)).join();

            var transformed = _rateService.ELTTransform(currentDate).join();
            if (!transformed) {
                log.warn("Transform failed for date {}", currentDate);
                return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", "Insert succeeded but transform failed"));
            }

            log.info("Rate ingested: {} on {}", request.getRate(), currentDate);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            log.error("Rate ingestion failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
