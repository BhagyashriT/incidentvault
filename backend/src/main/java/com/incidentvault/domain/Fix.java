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
@Table(name = "fixes")
public class Fix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "pr_url", length = 1024)
    private String prUrl;

    @Column(name = "commit_sha", length = 64)
    private String commitSha;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Fix() {
    }

    public Fix(Incident incident, String description, String prUrl, String commitSha) {
        this.incident = incident;
        this.description = description;
        this.prUrl = prUrl;
        this.commitSha = commitSha;
    }

    public Long getId() { return id; }
    public Incident getIncident() { return incident; }
    public String getDescription() { return description; }
    public String getPrUrl() { return prUrl; }
    public String getCommitSha() { return commitSha; }
    public Instant getCreatedAt() { return createdAt; }
}
