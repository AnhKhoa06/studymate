package com.studymate.common.web;

import com.studymate.user.entity.Role;

import java.util.Collections;
import java.util.List;

public final class MenuRegistry {

    private MenuRegistry() {
    }

    private static final List<MenuItem> STUDENT_MENUS = List.of(
            new MenuItem("Trang chủ", "/", "home", true, false),
            new MenuItem("Tìm bạn", "/matching", "search", true, true),
            new MenuItem("Nhóm", "/groups", "users", true, true),
            // "Phòng" (/rooms) đã bị bỏ; xem lại khi module Phòng tích hợp vào trang Đặt phòng
            new MenuItem("Đặt phòng", "/bookings", "calendar", true, true),
            new MenuItem("Chat", "/chat", "chat", true, true),
            new MenuItem("Thông báo", "/notifications", "bell", false, false),
            new MenuItem("Hồ sơ", "/profile", "user-circle", false, true)
    );

    private static final List<MenuItem> MANAGER_MENUS = List.of(
            new MenuItem("Quản lý phòng", "/manager/rooms", "door", true, false),
            new MenuItem("Duyệt đặt phòng", "/manager/bookings", "check", true, false),
            new MenuItem("Thống kê", "/manager/dashboard", "chart", true, false)
    );

    private static final List<MenuItem> ADMIN_MENUS = List.of(
            new MenuItem("Tài khoản", "/admin/users", "shield", true, false),
            new MenuItem("Nhật ký", "/admin/logs", "clock", true, false)
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
