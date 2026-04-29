package com.ecommerce.shop.repository;

import com.ecommerce.shop.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    boolean existsByEventId(String eventId);
    Optional<WebhookEvent> findByEventId(String eventId);
}
