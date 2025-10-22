package com.vinay.ratelimiter.worker.service.provider;

import com.vinay.ratelimiter.api.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class PushNotificationProvider implements NotificationProvider{

    @Override
    public void send(NotificationRequest request) {
        log.info("Sending push notification");
    }

    @Override
    public String getChannel() {
        log.info("Getting channel");
        return "PUSH_NOTIFICATION";
    }
}
