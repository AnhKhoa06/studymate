package com.studymate.room.dto;

import com.studymate.room.entity.BookingStatus;

import java.time.LocalDateTime;

public record BookingDto(
        Long id,
        Long roomId,
        String roomCode,
        String roomBuilding,
        Long groupId,
        Long requestedByUserId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String purpose,
        BookingStatus status,
        String rejectionReason,
        Long reviewedByUserId,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt
) {
}
