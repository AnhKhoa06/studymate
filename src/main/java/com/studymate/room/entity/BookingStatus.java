package com.studymate.room.entity;

import java.util.List;

public enum BookingStatus {
    PENDING("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    REJECTED("Đã từ chối"),
    CANCELLED("Đã hủy");

    // Cac trang thai van dang giu cho phong (khong cho dat trung)
    public static final List<BookingStatus> ACTIVE_STATUSES = List.of(PENDING, APPROVED);

    private final String displayName;

    BookingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isOccupyingRoom() {
        return this == PENDING || this == APPROVED;
    }
}
