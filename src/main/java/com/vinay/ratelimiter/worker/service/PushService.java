package com.vinay.ratelimiter.worker.service;

import com.vinay.ratelimiter.api.dto.NotificationRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class PushService {
    public void send(NotificationRequest notificationRequest) {
        log.info("Sending push notification to destination: {} with message: {}", notificationRequest, notificationRequest.message());
    }
}
