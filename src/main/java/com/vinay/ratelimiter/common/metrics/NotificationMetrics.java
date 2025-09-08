package com.vinay.ratelimiter.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class NotificationMetrics {

    private final MeterRegistry registry;

    // global counters
    private final Counter queuedCounter;
    private final Counter successCounter;
    private final Counter failedCounter;
    private final Counter retryCounter;

    // per-channel counters (cached)
    private final ConcurrentMap<String, Counter> queuedByChannel = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> successByChannel = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> failedByChannel = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> retryByChannel = new ConcurrentHashMap<>();

    public NotificationMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.queuedCounter = Counter.builder("notifications_queued_total")
                .description("Total number of notifications queued")
                .register(registry);
        this.successCounter = Counter.builder("notifications_success_total")
                .description("Total number of successfully sent notifications")
                .register(registry);
        this.failedCounter = Counter.builder("notifications_failed_total")
                .description("Total number of permanently failed notifications")
                .register(registry);
        this.retryCounter = Counter.builder("notifications_retry_total")
                .description("Total number of retried attempted")
                .register(registry);
    }

    public void incrementQueued(String channel) {
        queuedCounter.increment();
        getOrCreate(queuedByChannel,"notifications_queue",channel,"Queued notifications per channel").increment();
    }

    public void incrementSent(String channel) {
        successCounter.increment();
        getOrCreate(successByChannel,"notifications_success",channel,"Successfully sent notifications per channel").increment();
    }

    public void incrementFailed(String channel) {
        failedCounter.increment();
        getOrCreate(failedByChannel,"notifications_failed",channel,"Failed notifications per channel").increment();
    }

    public void incrementRetry(String channel) {
        retryCounter.increment();
        getOrCreate(retryByChannel,"notifications_retry",channel,"Retried notifications per channel").increment();
    }

    private Counter getOrCreate(ConcurrentMap<String, Counter> map, String metricName, String channel, String description) {
        String label = (channel == null ? "UNKNOWN" : channel.toUpperCase());
        String key = metricName + ":" + label;
        return map.computeIfAbsent(key,k->
                Counter.builder(metricName)
                .description(description)
                        .tag("channel",label)
                        .register(registry));
    }
}
