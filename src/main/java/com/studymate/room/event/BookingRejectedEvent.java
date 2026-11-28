package com.studymate.room.event;

import java.time.LocalDateTime;

public record BookingRejectedEvent(
        Long bookingId,
        Long roomId,
        String roomCode,
        Long groupId,
        Long requestedByUserId,
        Long rejectedByUserId,
        String reason,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
