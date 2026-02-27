package com.smiley.common;

import com.smiley.models.SymbolConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "ranalysis")
@Data
public class AppSetting {
    private int batchSize;
    private String eltFilePath;
    private String eltFileNameFormat;
    private List<SymbolConfig> symbols;
    private String notifyWebhookUrl;
}
