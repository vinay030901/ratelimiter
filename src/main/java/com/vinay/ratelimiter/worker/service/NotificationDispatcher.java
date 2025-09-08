package com.vinay.ratelimiter.worker.service;

import com.vinay.ratelimiter.api.dto.NotificationRequest;
import com.vinay.ratelimiter.common.enums.NotificationStatus;
import com.vinay.ratelimiter.common.metrics.NotificationMetrics;
import com.vinay.ratelimiter.common.model.NotificationEntity;
import com.vinay.ratelimiter.common.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class NotificationDispatcher {

    private final EmailService emailService;
    private final PushService pushService;
    private final SmsService smsService;
    private final NotificationMetrics metrics;
    private final NotificationRepository repository;

    public NotificationDispatcher(EmailService emailService, PushService pushService, SmsService smsService, NotificationMetrics metrics, NotificationRepository repository) {
        this.emailService = emailService;
        this.pushService = pushService;
        this.smsService = smsService;
        this.metrics = metrics;
        this.repository = repository;
    }

    public void dispatch(NotificationRequest request) {
        NotificationEntity entity = repository.findByRequestId(request.requestId()).orElseThrow(() -> new IllegalArgumentException("Notification not found in DB"));
        String channel = request.channel().toUpperCase();
        try {
            switch (channel) {
                case "EMAIL" -> emailService.send(request);
                case "PUSH" -> pushService.send(request);
                case "SMS" -> smsService.send(request);
                default -> {
                    entity.setStatus(NotificationStatus.FAILED);
                    entity.setErrorMessage("Unsupported channel: " + channel);
                    repository.save(entity);
                    return;
                }
            }
            entity.setStatus(NotificationStatus.SENT);
            entity.setDeliveredAt(Instant.now());
            repository.save(entity);
            metrics.incrementSent(channel);

        } catch (Exception e) {
            entity.setStatus(NotificationStatus.FAILED);
            entity.setErrorMessage(e.getMessage());
            repository.save(entity);
            metrics.incrementFailed(channel);
            throw e;
        }
    }
}
