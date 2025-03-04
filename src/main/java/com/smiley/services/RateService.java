package com.smiley.services;
import com.smiley.common.AppSetting;
import com.smiley.entities.RateEntity;
import com.smiley.helpers.DateHelper;
import com.smiley.helpers.FileHelper;
import com.smiley.repositories.RateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class RateService {

    private final RateRepository _repository;
    private final AppSetting _appSetting;

    public RateService(RateRepository repository, AppSetting appSetting) {
        this._repository = repository;
        this._appSetting = appSetting;
    }

    // core logic here
    public CompletableFuture<Boolean> ELTLoadCsv(LocalDate currentDate){
        // read csv path (base path + date)
        var basePath = Paths.get(_appSetting.getEltFilePath());
        var fileNameFormat = _appSetting.getEltFileNameFormat();
        var fullPath = basePath.resolve(fileNameFormat.replace("{{DATE}}", DateHelper.ToDateString(currentDate)));

        if(!Files.exists(fullPath)){
            return CompletableFuture.completedFuture(true);
        }

        var csvModel = FileHelper.ReadContent(fullPath);

        // form into RateDTO
        var rates = csvModel.stream().map(x -> {
            var rateDto = new RateEntity();
            x.Columns.forEach(y -> {
                switch(y.ColumnIndex){
                    case 0:
                        // dateRetrieved
                        rateDto.setDateRetrieved(DateHelper.ParseDate((y.Value)));
                        break;
                    case 1:
                        // rate
                        rateDto.setRate(Double.parseDouble(y.Value));
                        break;
                }
            });
            return rateDto;
        }).toList();

        // insert into postgres
        var future = _repository.insertRatesAsync(rates);
        var insertResult = future.join();

        if(!insertResult){
            // logger to record message
            log.error("Rates insert failed");
        }

        return CompletableFuture.completedFuture(insertResult);
    }

    public CompletableFuture<Boolean> ELTTransform(LocalDate currentDate){
        Boolean result = false;
        try{
            // agg daily
            result = _repository.aggregateRateByDaily(currentDate).join();

            if(!result) {
                throw new Exception("Daily job failed");
            }

            // agg monthly
            var firstDayOfMonth = currentDate.withDayOfMonth(1);
            var lastDayOfMonth = currentDate.plusMonths(1).withDayOfMonth(1);
            result = _repository.aggregateRateByMonthly(firstDayOfMonth, lastDayOfMonth).join();

            if(!result) {
                throw new Exception("Monthly job failed");
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return CompletableFuture.completedFuture(result);
    }
}
