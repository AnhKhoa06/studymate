package com.studymate.user.dto;

import java.time.LocalDateTime;

public record ActivityLogDto(
        Long id,
        Long userId,
        String actorEmail,
        String action,
        String detail,
        LocalDateTime createdAt
) {
}
