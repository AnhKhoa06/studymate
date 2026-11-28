package com.studymate.user.dto;

import com.studymate.user.entity.Role;

import java.time.LocalDateTime;

public record AdminUserDto(
        Long id,
        String email,
        String fullName,
        Role role,
        boolean enabled,
        LocalDateTime createdAt
) {
}
