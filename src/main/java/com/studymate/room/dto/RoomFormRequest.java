package com.studymate.room.dto;

import com.studymate.room.entity.RoomStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomFormRequest {

    @NotBlank(message = "Mã phòng không được để trống")
    @Size(max = 30, message = "Mã phòng tối đa 30 ký tự")
    private String code;

    @NotBlank(message = "Tòa nhà không được để trống")
    @Size(max = 50, message = "Tên tòa nhà tối đa 50 ký tự")
    private String building;

    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa phải lớn hơn 0")
    @Max(value = 500, message = "Sức chứa tối đa 500 chỗ")
    private Integer capacity;

    @Size(max = 255, message = "Mô tả thiết bị tối đa 255 ký tự")
    private String equipment = "";

    @NotNull(message = "Trạng thái phòng không được để trống")
    private RoomStatus status = RoomStatus.AVAILABLE;
}
