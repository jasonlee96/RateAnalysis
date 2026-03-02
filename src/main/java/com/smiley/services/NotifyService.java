package com.smiley.services;

import com.smiley.common.AppSetting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        _restClient = RestClient.builder().requestFactory(factory).build();
    }

    public void send(String message) {
        var url = _appSetting.getNotifyWebhookUrl();
        if (url == null || url.isBlank()) {
            log.info("Notify webhook URL not configured, skipping notification");
        } else {
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
        sendPush("Rate Alert", message);
    }

    private void sendPush(String title, String body) {
        var apiUrl = _appSetting.getSmileyApiUrl();
        var key = _appSetting.getSmileyInternalKey();
        if (apiUrl == null || apiUrl.isBlank() || key == null || key.isBlank()) {
            log.info("Smiley push not configured, skipping");
            return;
        }
        try {
            _restClient.post()
                    .uri(apiUrl + "/notifications/push")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-internal-key", key)
                    .body(Map.of("title", title, "body", body))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Push notification sent: {}", body);
        } catch (Exception e) {
            log.error("Failed to send push notification: {}", e.getMessage());
        }
    }
}
