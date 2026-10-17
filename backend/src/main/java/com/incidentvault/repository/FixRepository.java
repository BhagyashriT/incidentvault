package com.incidentvault.repository;

import com.incidentvault.domain.Fix;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FixRepository extends JpaRepository<Fix, Long> {

    List<Fix> findByIncidentIdOrderByIdAsc(Long incidentId);
}
