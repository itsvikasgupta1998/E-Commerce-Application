package com.app.entites;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "processed_webhooks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_processed_webhook_event_id",
                        columnNames = "event_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedWebhook extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "event_id",
            nullable = false,
            unique = true,
            length = 100
    )
    private String eventId;

    @Column(nullable = false, length = 100)
    private String eventType;
}