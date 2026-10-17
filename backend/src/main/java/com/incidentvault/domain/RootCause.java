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
@Table(name = "root_causes")
public class RootCause {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String hypothesis;

    @Column(nullable = false)
    private boolean validated = false;

    @Column(columnDefinition = "TEXT")
    private String evidence;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RootCause() {
    }

    public RootCause(Incident incident, String hypothesis, boolean validated, String evidence) {
        this.incident = incident;
        this.hypothesis = hypothesis;
        this.validated = validated;
        this.evidence = evidence;
    }

    public Long getId() { return id; }
    public Incident getIncident() { return incident; }
    public String getHypothesis() { return hypothesis; }
    public boolean isValidated() { return validated; }
    public String getEvidence() { return evidence; }
    public Instant getCreatedAt() { return createdAt; }
}
