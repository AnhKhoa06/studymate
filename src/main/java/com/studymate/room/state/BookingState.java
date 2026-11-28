package com.studymate.room.state;

import com.studymate.room.entity.Booking;
import com.studymate.room.entity.BookingStatus;

import java.time.LocalDateTime;

// State pattern cho vong doi dat phong: moi trang thai quy dinh cac chuyen doi hop le
public interface BookingState {

    BookingStatus getStatus();

    default void approve(Booking booking, Long reviewerUserId, LocalDateTime now) {
        throw new InvalidBookingStateException(
                "Không thể duyệt đặt phòng đang ở trạng thái " + getStatus().getDisplayName());
    }

    default void reject(Booking booking, Long reviewerUserId, String reason, LocalDateTime now) {
        throw new InvalidBookingStateException(
                "Không thể từ chối đặt phòng đang ở trạng thái " + getStatus().getDisplayName());
    }

    default void cancel(Booking booking, LocalDateTime now) {
        throw new InvalidBookingStateException(
                "Không thể hủy đặt phòng đang ở trạng thái " + getStatus().getDisplayName());
    }
}
