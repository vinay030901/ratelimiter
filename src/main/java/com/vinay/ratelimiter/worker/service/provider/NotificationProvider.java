package com.vinay.ratelimiter.worker.service.provider;

import com.vinay.ratelimiter.api.dto.NotificationRequest;

public interface NotificationProvider {
    void send(NotificationRequest request);

    String getChannel(); // e.g. "EMAIL", "SMS", "PUSH"
}
