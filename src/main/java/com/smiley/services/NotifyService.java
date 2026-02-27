package com.smiley.services;

import com.smiley.common.AppSetting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
public class NotifyService {

    private final AppSetting _appSetting;
    private final RestClient _restClient;

    public NotifyService(AppSetting appSetting) {
        _appSetting = appSetting;
        _restClient = RestClient.create();
    }

    public void send(String message) {
        var url = _appSetting.getNotifyWebhookUrl();
        if (url == null || url.isBlank()) {
            log.info("Notify webhook URL not configured, skipping notification");
            return;
        }
        try {
            _restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("msg", message))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Notification sent: {}", message);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }
}
