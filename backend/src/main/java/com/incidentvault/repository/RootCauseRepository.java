package com.incidentvault.repository;

import com.incidentvault.domain.RootCause;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RootCauseRepository extends JpaRepository<RootCause, Long> {

    List<RootCause> findByIncidentIdOrderByIdAsc(Long incidentId);
}
