package com.vinay.ratelimiter.worker.consumer;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import com.vinay.ratelimiter.api.dto.NotificationRequest;
import com.vinay.ratelimiter.common.enums.NotificationStatus;
import com.vinay.ratelimiter.common.metrics.NotificationMetrics;
import com.vinay.ratelimiter.common.repository.NotificationRepository;
import com.vinay.ratelimiter.worker.service.NotificationDispatcher;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class NotificationConsumer {

    private final NotificationDispatcher dispatcher;
    private final NotificationRepository repository;
    private final NotificationMetrics notificationMetrics;

    public NotificationConsumer(NotificationDispatcher dispatcher, NotificationRepository repository,
            NotificationMetrics notificationMetrics) {
        this.dispatcher = dispatcher;
        this.repository = repository;

        this.notificationMetrics = notificationMetrics;
    }

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 8000), dltTopicSuffix = ".dlt", autoCreateTopics = "false", dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "notifications.requests", groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(NotificationRequest request) {
        try {
                log.debug("Consumer successfully deserialized NotificationRequest: {}", request);
                dispatcher.dispatch(request);
                notificationMetrics.incrementSent(request.channel());
        } catch (Exception e) {
            log.error("Consumer exception: {}", e.getMessage(), e);
            // Optionally, handle DB retry logic here if payload is NotificationRequest
                repository.findByRequestId(request.requestId()).ifPresent(entity -> {
                    entity.setStatus(NotificationStatus.RETRYING);
                    entity.setRetryCount((entity.getRetryCount() == null ? 0 : entity.getRetryCount()) + 1);
                    entity.setErrorMessage(e.getMessage());
                    entity.setLastAttemptAt(Instant.now());
                    repository.save(entity);
                });
                log.warn("Retrying notification {} due to error: {}", request.requestId(), e.getMessage());
                notificationMetrics.incrementRetry(request.channel());
            throw e; // rethrow to trigger retry/DLT
        }
    }

    // consumes from DLT after retries are exhausted
    @KafkaListener(topics = "notifications.requests.dlt", groupId = "notification-dlt-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeDlt(NotificationRequest request,
            @Header(name = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exceptionMessage,
            @Header(name = KafkaHeaders.DLT_EXCEPTION_STACKTRACE, required = false) byte[] exceptionStack) {
        String msg = exceptionMessage != null ? exceptionMessage : "Unknown error";
        String stack = exceptionStack != null ? new String(exceptionStack, StandardCharsets.UTF_8) : "";
        log.error("Permanently failed notification {} due to error: {}, stacktrace: {}", request.requestId(), msg,
                stack);
        repository.findByRequestId(request.requestId()).ifPresent(entity -> {
            entity.setStatus(NotificationStatus.FAILED);
            entity.setErrorMessage(msg);
            entity.setLastAttemptAt(Instant.now());
            repository.save(entity);
        });
    }
}
