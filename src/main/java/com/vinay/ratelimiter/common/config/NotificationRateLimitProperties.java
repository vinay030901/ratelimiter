package com.vinay.ratelimiter.common.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "notification.limits")
@Data
public class NotificationRateLimitProperties {

    private Map<String, LimitConfig> channels=new HashMap<>();

    @Data
    public static class LimitConfig {
        private int limit;
        private int window;
    }
}