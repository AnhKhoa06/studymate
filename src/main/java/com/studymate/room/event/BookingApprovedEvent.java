package com.studymate.room.event;

import java.time.LocalDateTime;

public record BookingApprovedEvent(
        Long bookingId,
        Long roomId,
        String roomCode,
        Long groupId,
        Long requestedByUserId,
        Long approvedByUserId,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
