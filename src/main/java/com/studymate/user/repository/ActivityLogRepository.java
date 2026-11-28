package com.studymate.user.repository;

import com.studymate.user.dto.ActivityLogDto;
import com.studymate.user.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @Query("""
            SELECT new com.studymate.user.dto.ActivityLogDto(
                l.id, l.userId, l.actorEmail, l.action, l.detail, l.createdAt
            )
            FROM ActivityLog l
            WHERE (:keyword IS NULL OR LOWER(l.actorEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(l.detail) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:action IS NULL OR l.action = :action)
            """)
    Page<ActivityLogDto> searchLogs(@Param("keyword") String keyword,
                                    @Param("action") String action,
                                    Pageable pageable);
}
