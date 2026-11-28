package com.studymate.room.entity;

public enum RoomStatus {
    AVAILABLE("Sẵn sàng"),
    MAINTENANCE("Bảo trì"),
    INACTIVE("Ngưng sử dụng");

    private final String displayName;

    RoomStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
