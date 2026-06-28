package com.app.repositories;

import com.app.entites.ProcessedWebhook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedWebhookRepository
        extends JpaRepository<ProcessedWebhook, Long> {

    boolean existsByEventId(String eventId);
}