package com.vinay.ratelimiter.worker.service;

import com.vinay.ratelimiter.api.dto.NotificationRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class EmailService {
    public void send(NotificationRequest notificationRequest) {
        log.info("Sending email to {} with message: {}", notificationRequest.destination(), notificationRequest.message());
    }
}
