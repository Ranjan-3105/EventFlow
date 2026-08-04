package com.eventflow.eventflow.repository;

import com.eventflow.eventflow.entity.Event;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;


public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("""
        SELECT COUNT(e) > 0
        FROM Event e
        WHERE e.hall.id = :hallId
          AND e.status <> 'CANCELLED'
          AND e.startTime < :endTime
          AND e.endTime > :startTime
    """)
    boolean existsConflictingEvent(
            @Param("hallId") UUID hallId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );
}