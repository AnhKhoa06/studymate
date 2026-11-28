package com.studymate.user.dto;

import com.studymate.user.entity.AcademicLevel;
import com.studymate.user.entity.Role;
import com.studymate.user.entity.StudyGoal;

import java.time.LocalDateTime;
import java.util.List;

public record ProfileViewDto(
        Long userId,
        String fullName,
        String email,
        Role role,
        String faculty,
        String className,
        AcademicLevel academicLevel,
        StudyGoal studyGoal,
        String bio,
        List<SubjectDto> subjects,
        List<AvailabilitySlotDto> availabilitySlots,
        LocalDateTime updatedAt
) {
}
