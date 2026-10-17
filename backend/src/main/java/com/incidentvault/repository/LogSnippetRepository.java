package com.incidentvault.repository;

import com.incidentvault.domain.LogSnippet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogSnippetRepository extends JpaRepository<LogSnippet, Long> {

    List<LogSnippet> findByIncidentIdOrderByCapturedAtAscIdAsc(Long incidentId);
}
