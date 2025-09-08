package com.vinay.ratelimiter.common.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Log4j2
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String userId, String channel, int limit, int window) {
        String key = String.format("rate_limit:%s:%s", userId, channel);
        Long count = redisTemplate.opsForValue().increment(key);
        log.info("key: {}, count: {}", key, count);
        if (count != null && count == 1) {
            // first time -> set ttl
            redisTemplate.expire(key, Duration.ofSeconds(window));
        }
        return count != null && count <= limit;
    }
}
