package com.incidentvault.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "timeline_events")
public class TimelineEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    private String actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TimelineEventType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TimelineEvent() {
    }

    public TimelineEvent(Incident incident, Instant timestamp, String actor,
                         TimelineEventType type, String message) {
        this.incident = incident;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.actor = actor;
        this.type = type;
        this.message = message;
    }

    public Long getId() { return id; }
    public Incident getIncident() { return incident; }
    public Instant getTimestamp() { return timestamp; }
    public String getActor() { return actor; }
    public TimelineEventType getType() { return type; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
}
