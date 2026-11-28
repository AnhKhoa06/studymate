package com.studymate.room.dto;

import com.studymate.room.entity.RoomStatus;

public record RoomDto(
        Long id,
        String code,
        String building,
        int capacity,
        String equipment,
        RoomStatus status
) {
}
