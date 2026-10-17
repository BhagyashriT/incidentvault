package com.incidentvault.web.dto;

import com.incidentvault.domain.TimelineEvent;
import com.incidentvault.domain.TimelineEventType;

import java.time.Instant;

public record TimelineEventDto(
        Long id,
        Instant timestamp,
        String actor,
        TimelineEventType type,
        String message) {

    public static TimelineEventDto from(TimelineEvent e) {
        return new TimelineEventDto(e.getId(), e.getTimestamp(), e.getActor(), e.getType(), e.getMessage());
    }
}
