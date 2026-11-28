package com.studymate.user.service;

import com.studymate.user.dto.ActivityLogDto;
import com.studymate.user.entity.ActivityLog;
import com.studymate.user.repository.ActivityLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional
    public void record(Long userId, String actorEmail, String action, String detail) {
        ActivityLog log = new ActivityLog();
        log.setUserId(userId);
        log.setActorEmail(actorEmail);
        log.setAction(action);
        log.setDetail(detail);
        log.setCreatedAt(LocalDateTime.now());
        activityLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ActivityLogDto> findLogs(String keyword, String action, Pageable pageable) {
        String normalizedKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        String normalizedAction = (action == null || action.trim().isEmpty()) ? null : action.trim();
        return activityLogRepository.searchLogs(normalizedKeyword, normalizedAction, pageable);
    }
}
