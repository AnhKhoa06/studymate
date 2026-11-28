package com.studymate.user.dto;

import com.studymate.user.entity.AcademicLevel;
import com.studymate.user.entity.StudyGoal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProfileUpdateRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    @Size(max = 100, message = "Khoa tối đa 100 ký tự")
    private String faculty;

    @Size(max = 50, message = "Lớp tối đa 50 ký tự")
    private String className;

    private AcademicLevel academicLevel;

    private StudyGoal studyGoal;

    @Size(max = 500, message = "Giới thiệu tối đa 500 ký tự")
    private String bio;

    @Valid
    private List<SubjectDto> subjects = new ArrayList<>();

    @Valid
    private List<AvailabilitySlotDto> availabilitySlots = new ArrayList<>();
}
