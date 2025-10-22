package com.vinay.ratelimiter.api.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vinay.ratelimiter.api.dto.NotificationRequest;
import com.vinay.ratelimiter.api.service.NotificationProducerService;

import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/notifications")
@Log4j2
public class NotificationController {

    private final NotificationProducerService producerService;

    public NotificationController(NotificationProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> sendNotification(
            @Valid @RequestBody NotificationRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        log.debug("Received notification request: {}", request);
        Map<String, Object> response = producerService.handleNotification(request, idempotencyKey);
        log.debug("Notification response: {}", response);
        return ResponseEntity.status((HttpStatus) response.getOrDefault("httpStatus", HttpStatus.OK))
                .body(response);
    }
}
