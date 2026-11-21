package com.studymate.common.web;

import com.studymate.user.entity.Role;

import java.util.Collections;
import java.util.List;

public final class MenuRegistry {

    private MenuRegistry() {
    }

    private static final List<MenuItem> STUDENT_MENUS = List.of(
            new MenuItem("Tìm bạn", "/matching", "magnifying-glass", true, true),
            new MenuItem("Nhóm", "/groups", "users", true, true),
            new MenuItem("Phòng", "/rooms", "calendar-blank", true, false),
            new MenuItem("Đặt phòng", "/bookings", "calendar-blank", true, true),
            new MenuItem("Chat", "/chat", "chat-circle", true, true),
            new MenuItem("Thông báo", "/notifications", "bell", true, false),
            new MenuItem("Hồ sơ", "/profile", "user-circle", true, true)
    );

    private static final List<MenuItem> MANAGER_MENUS = List.of(
            new MenuItem("Quản lý phòng", "/manager/rooms", "calendar-blank", true, false),
            new MenuItem("Duyệt đặt phòng", "/manager/bookings", "check", true, false),
            new MenuItem("Thống kê", "/manager/dashboard", "list", true, false)
    );

    private static final List<MenuItem> ADMIN_MENUS = List.of(
            new MenuItem("Tài khoản", "/admin/users", "users", true, false)
    );

    public static List<MenuItem> getMenuItems(Role role) {
        if (role == null) {
            return Collections.emptyList();
        }
        return switch (role) {
            case STUDENT -> STUDENT_MENUS;
            case MANAGER -> MANAGER_MENUS;
            case ADMIN -> ADMIN_MENUS;
        };
    }
}
