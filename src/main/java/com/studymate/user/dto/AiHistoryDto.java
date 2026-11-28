package com.studymate.user.dto;

import com.studymate.user.entity.AiRequestType;

import java.time.LocalDateTime;

public record AiHistoryDto(
        Long id,
        AiRequestType type,
        String promptSummary,
        String responseText,
        boolean fallbackUsed,
        LocalDateTime createdAt
) {
}
