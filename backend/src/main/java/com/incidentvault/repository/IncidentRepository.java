package com.incidentvault.repository;

import com.incidentvault.domain.Incident;
import com.incidentvault.domain.IncidentStatus;
import com.incidentvault.domain.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findAllByOrderByCreatedAtDesc();

    /**
     * Postgres full-text search over the generated tsvector column, ordered by relevance.
     * NULL-tolerant on every filter; enums are passed as their String names.
     */
    @Query(value = """
            SELECT * FROM incidents
            WHERE search_vector @@ plainto_tsquery('english', CAST(:q AS text))
              AND (CAST(:severity AS varchar) IS NULL OR severity = CAST(:severity AS varchar))
              AND (CAST(:service AS varchar) IS NULL OR lower(service) = lower(CAST(:service AS varchar)))
              AND (CAST(:status AS varchar) IS NULL OR status = CAST(:status AS varchar))
            ORDER BY ts_rank(search_vector, plainto_tsquery('english', CAST(:q AS text))) DESC,
                     created_at DESC
            """, nativeQuery = true)
    List<Incident> searchFullText(@Param("q") String q,
                                  @Param("severity") String severity,
                                  @Param("service") String service,
                                  @Param("status") String status);

    /**
     * Portable fallback (e.g. H2 in tests): case-insensitive LIKE over the same fields.
     */
    @Query("""
            SELECT i FROM Incident i
            WHERE (:q IS NULL
                   OR lower(i.title) LIKE lower(concat('%', :q, '%'))
                   OR lower(i.summary) LIKE lower(concat('%', :q, '%'))
                   OR lower(i.service) LIKE lower(concat('%', :q, '%')))
              AND (:severity IS NULL OR i.severity = :severity)
              AND (:service IS NULL OR lower(i.service) = lower(:service))
              AND (:status IS NULL OR i.status = :status)
            ORDER BY i.createdAt DESC
            """)
    List<Incident> searchPortable(@Param("q") String q,
                                  @Param("severity") Severity severity,
                                  @Param("service") String service,
                                  @Param("status") IncidentStatus status);
}
