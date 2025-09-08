package com.vinay.ratelimiter.api.controller;

import com.vinay.ratelimiter.api.dto.NotificationRequest;
import com.vinay.ratelimiter.api.service.NotificationProducerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationProducerService producerService;

    public NotificationController(NotificationProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> sendNotification(
            @Valid @RequestBody NotificationRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        Map<String, Object> response = producerService.handleNotification(request, idempotencyKey);
        return ResponseEntity.status((HttpStatus) response.getOrDefault("httpStatus", HttpStatus.OK))
                .body(response);
    }
}
