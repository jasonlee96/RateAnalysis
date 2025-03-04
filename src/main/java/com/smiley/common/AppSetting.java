package com.smiley.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "ranalysis")
@Data // Lombok annotation to auto-generate getters and setters
public class AppSetting {
    private int batchSize;
    private String eltFilePath;
    private String eltFileNameFormat;
}
