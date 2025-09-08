package com.vinay.ratelimiter.api.service;


import com.vinay.ratelimiter.api.dto.NotificationRequest;
import com.vinay.ratelimiter.common.config.NotificationRateLimitProperties;
import com.vinay.ratelimiter.common.metrics.NotificationMetrics;
import com.vinay.ratelimiter.common.model.NotificationEntity;
import com.vinay.ratelimiter.common.enums.NotificationStatus;
import com.vinay.ratelimiter.common.repository.NotificationRepository;
import com.vinay.ratelimiter.common.service.RateLimiterService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationRateLimitProperties rateLimitProperties;
    private final RateLimiterService rateLimiterService;
    private final NotificationMetrics notificationMetrics;
    private final NotificationRepository repository;

    public NotificationProducerService(KafkaTemplate<String, Object> kafkaTemplate,
                                       RedisTemplate<String, String> redisTemplate,
                                       NotificationRateLimitProperties rateLimitProperties,
                                       RateLimiterService rateLimiterService, NotificationMetrics notificationMetrics,
                                       NotificationRepository repository) {
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
        this.rateLimitProperties = rateLimitProperties;
        this.rateLimiterService = rateLimiterService;
        this.notificationMetrics = notificationMetrics;
        this.repository = repository;
    }

    public Map<String, Object> handleNotification(NotificationRequest request, String idempotencyKey) {
        String reqId = (request.requestId() == null || request.requestId().isBlank())
                ? UUID.randomUUID().toString()
                : request.requestId();

        NotificationRequest enrichedRequest =
                new NotificationRequest(reqId, request.userId(), request.channel(), request.destination(), request.message());

        // idempotency
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            String idemKey = String.format("i-dempotency:%s:%s", enrichedRequest.userId(), idempotencyKey);
            Boolean alreadyExists = redisTemplate.hasKey(idemKey);
            if (alreadyExists != null && alreadyExists) {
                return Map.of(
                        "status", "duplicate",
                        "requestId", reqId,
                        "httpStatus", HttpStatus.OK
                );
            }
            redisTemplate.opsForValue().set(idemKey, reqId, 1, TimeUnit.HOURS);
        }

        // rate limiting
        var config = rateLimitProperties.getChannels()
                .getOrDefault(enrichedRequest.channel().toUpperCase(), new NotificationRateLimitProperties.LimitConfig());
        int limit = config.getLimit() > 0 ? config.getLimit() : 5;
        int window = config.getWindow() > 0 ? config.getWindow() : 60;

        boolean allowed = rateLimiterService.isAllowed(request.userId(), request.channel(), limit, window);
        if (!allowed) {
            notificationMetrics.incrementRetry(enrichedRequest.channel());
            return Map.of(
                    "status", "rate_limited",
                    "requestId", reqId,
                    "httpStatus", HttpStatus.TOO_MANY_REQUESTS
            );
        }

        // persist QUEUED in DB
        NotificationEntity entity = new NotificationEntity();
        entity.setRequestId(reqId);
        entity.setUserId(request.userId());
        entity.setChannel(request.channel());
        entity.setDestination(request.destination());
        entity.setMessage(request.message());
        entity.setStatus(NotificationStatus.QUEUED);
        entity.setCreatedAt(Instant.now());
        repository.save(entity);

        // enqueue in Kafka
        kafkaTemplate.send("notifications.requests", reqId, enrichedRequest);

        notificationMetrics.incrementQueued(enrichedRequest.channel());

        return Map.of(
                "status", "queued",
                "requestId", reqId,
                "httpStatus", HttpStatus.OK
        );
    }
}
