package com.incidentvault.web.dto;

import com.incidentvault.domain.LogSnippet;

import java.time.Instant;

public record LogSnippetDto(
        Long id,
        String source,
        String snippet,
        Instant capturedAt,
        Instant createdAt) {

    public static LogSnippetDto from(LogSnippet l) {
        return new LogSnippetDto(l.getId(), l.getSource(), l.getSnippet(), l.getCapturedAt(), l.getCreatedAt());
    }
}
