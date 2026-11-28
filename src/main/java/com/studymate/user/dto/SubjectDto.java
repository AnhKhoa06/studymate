package com.studymate.user.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDto {

    @Size(max = 30, message = "Mã môn tối đa 30 ký tự")
    private String subjectCode;

    @Size(max = 120, message = "Tên môn tối đa 120 ký tự")
    private String subjectName;
}
