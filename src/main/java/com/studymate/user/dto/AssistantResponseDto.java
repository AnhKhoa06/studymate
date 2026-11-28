package com.studymate.user.dto;

import com.studymate.user.entity.AiRequestType;

import java.time.LocalDateTime;

public record AssistantResponseDto(
        AiRequestType type,
        String answer,
        boolean fallbackUsed,
        boolean rateLimited,
        LocalDateTime createdAt
) {
}
