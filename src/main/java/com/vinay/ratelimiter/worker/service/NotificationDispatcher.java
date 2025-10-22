package com.vinay.ratelimiter.worker.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.vinay.ratelimiter.api.dto.NotificationRequest;
import com.vinay.ratelimiter.common.enums.NotificationStatus;
import com.vinay.ratelimiter.common.metrics.NotificationMetrics;
import com.vinay.ratelimiter.common.model.NotificationEntity;
import com.vinay.ratelimiter.common.repository.NotificationRepository;
import com.vinay.ratelimiter.worker.service.provider.NotificationProvider;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class NotificationDispatcher {

    private final Map<String, NotificationProvider> providers;
    private final NotificationRepository repository;
    private final NotificationMetrics metrics;

    public NotificationDispatcher(List<NotificationProvider> providers,
            NotificationRepository repository,
            NotificationMetrics metrics) {
        this.providers = providers.stream()
                .collect(Collectors.toMap(
                        p -> p.getChannel().toUpperCase(),
                        p -> p));
        log.debug("Registered providers: {}",
                this.providers.entrySet().stream()
                        .map(e -> e.getKey() + " -> " + e.getValue().getClass().getSimpleName())
                        .collect(Collectors.joining(", ")));
        this.repository = repository;
        this.metrics = metrics;
    }

    public void dispatch(NotificationRequest request) {
        NotificationEntity entity = repository.findByRequestId(request.requestId())
                .orElseThrow(() -> new IllegalArgumentException("Notification not found in DB"));
        String channel = request.channel().toUpperCase();

        NotificationProvider provider = providers.get(channel);
        if (provider == null) {
            entity.setStatus(NotificationStatus.FAILED);
            entity.setErrorMessage("Unsupported channel: " + channel);
            repository.save(entity);
            return;
        }

        try {
            log.info("Dispatching notification {} to channel {}", request.requestId(), channel);
            provider.send(request);
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
