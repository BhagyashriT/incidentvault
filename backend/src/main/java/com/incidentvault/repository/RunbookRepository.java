package com.incidentvault.repository;

import com.incidentvault.domain.Runbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RunbookRepository extends JpaRepository<Runbook, Long> {

    List<Runbook> findAllByOrderByCreatedAtDesc();

    List<Runbook> findByLinkedIncidentsId(Long incidentId);

    /**
     * Postgres full-text search over the generated tsvector column, ordered by relevance.
     */
    @Query(value = """
            SELECT * FROM runbooks
            WHERE search_vector @@ plainto_tsquery('english', CAST(:q AS text))
            ORDER BY ts_rank(search_vector, plainto_tsquery('english', CAST(:q AS text))) DESC,
                     created_at DESC
            """, nativeQuery = true)
    List<Runbook> searchFullText(@Param("q") String q);
}
