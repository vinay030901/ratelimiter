package com.vinay.ratelimiter.common.model;

import java.time.Instant;

import com.vinay.ratelimiter.common.enums.NotificationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String requestId;

    private String userId;
    private String channel;
    private String destination;

    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private Instant createdAt;
    private Instant deliveredAt;

    @Column(length = 500)
    private String errorMessage;

    private String providerMessageId;

    @Builder.Default
    private Integer retryCount = 0;
    private Instant lastAttemptAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.status == null)
            this.status = NotificationStatus.QUEUED;
    }
}
