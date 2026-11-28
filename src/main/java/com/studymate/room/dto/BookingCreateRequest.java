package com.studymate.room.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingCreateRequest {

    @NotNull(message = "Vui lòng chọn phòng học")
    private Long roomId;

    @NotNull(message = "Vui lòng chọn nhóm học")
    private Long groupId;

    @NotNull(message = "Vui lòng chọn thời gian bắt đầu")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @NotNull(message = "Vui lòng chọn thời gian kết thúc")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    @Size(max = 255, message = "Mục đích buổi học tối đa 255 ký tự")
    private String purpose = "";
}
