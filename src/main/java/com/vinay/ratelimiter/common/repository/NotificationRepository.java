package com.vinay.ratelimiter.common.repository;

import com.vinay.ratelimiter.common.model.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<NotificationEntity,Long> {

    Optional<NotificationEntity> findByRequestId(String requestId);
}
