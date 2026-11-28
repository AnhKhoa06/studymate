package com.studymate.room.event;

import java.time.LocalDateTime;

public record BookingCancelledEvent(
        Long bookingId,
        Long roomId,
        String roomCode,
        Long groupId,
        Long cancelledByUserId,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
