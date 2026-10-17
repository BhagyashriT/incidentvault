package com.incidentvault.repository;

import com.incidentvault.domain.TimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimelineEventRepository extends JpaRepository<TimelineEvent, Long> {

    List<TimelineEvent> findByIncidentIdOrderByTimestampAsc(Long incidentId);
}
