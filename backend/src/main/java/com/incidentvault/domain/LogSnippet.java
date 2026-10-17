package com.incidentvault.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "log_snippets")
public class LogSnippet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    private String source;

    @Column(columnDefinition = "TEXT")
    private String snippet;

    @Column(name = "captured_at")
    private Instant capturedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LogSnippet() {
    }

    public LogSnippet(Incident incident, String source, String snippet, Instant capturedAt) {
        this.incident = incident;
        this.source = source;
        this.snippet = snippet;
        this.capturedAt = capturedAt;
    }

    public Long getId() { return id; }
    public Incident getIncident() { return incident; }
    public String getSource() { return source; }
    public String getSnippet() { return snippet; }
    public Instant getCapturedAt() { return capturedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
