package com.smiley.main;

import com.smiley.common.AppSetting;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.smiley")
@EntityScan( basePackages = {"com.smiley.entities"} )
@EnableConfigurationProperties(AppSetting.class)
@EnableScheduling
@EnableAsync
public class RateAnalysisApplication {

	public static void main(String[] args) {
		SpringApplication.run(RateAnalysisApplication.class, args);
	}

	// TODO build and deploy to window services
	// TODO: 3. some data mining logic to extract which hours usually have better rates (etc. insights)

}
